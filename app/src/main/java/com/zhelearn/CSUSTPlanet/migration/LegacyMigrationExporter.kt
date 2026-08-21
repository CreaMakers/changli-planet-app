package com.zhelearn.CSUSTPlanet.migration

import android.content.Context
import com.zhelearn.CSUSTPlanet.BuildConfig
import com.zhelearn.CSUSTPlanet.feature.common.data.local.room.database.CoursesDataBase
import com.zhelearn.CSUSTPlanet.feature.ledger.data.local.room.database.AccountBookDatabase
import com.zhelearn.CSUSTPlanet.skin.data.cache.SkinCache
import java.io.File

class LegacyMigrationExporter(context: Context) {
    private val context = context.applicationContext
    fun createSnapshot(migrationId: String): File {
        val courses = CoursesDataBase.getDatabase(context).courseDao().getAllCustomCourses()
            .map { course ->
                MigrationCourse(
                    courseName = course.courseName,
                    classroom = course.classroom.orEmpty(),
                    teacher = course.teacher,
                    weeks = course.weeks.orEmpty(),
                    start = course.start,
                    step = course.step,
                    weekday = course.weekday,
                    term = course.term,
                    studentId = course.studentId
                )
            }
        val ledgerItems = AccountBookDatabase.getInstance(context).accountBookDao()
            .getAllSomethingItems()
            .map { item ->
                MigrationLedgerItem(
                    sourceId = item.id,
                    name = item.name,
                    totalMoney = item.totalMoney,
                    dailyAverage = item.dailyAverage,
                    startTime = item.startTime,
                    picture = item.picture,
                    username = item.username
                )
            }
        val payload = MigrationPayload(
            customCourses = courses,
            ledgerItems = ledgerItems,
            preferences = MigrationPreferences(
                skinName = SkinCache.getAssetsName(),
                activeSkinName = SkinCache.getIsUsingSkin()
            )
        )
        val archive = MigrationArchive(
            migrationId = migrationId,
            sourceVersionCode = BuildConfig.VERSION_CODE,
            createdAt = System.currentTimeMillis(),
            payloadDigest = MigrationCodec.payloadDigest(payload),
            payload = payload
        )
        val archiveBytes = MigrationCodec.encode(archive)
        require(archiveBytes.size <= MigrationContract.MAX_ARCHIVE_BYTES) { "Migration archive is too large" }
        val migrationDirectory = File(context.cacheDir, "migration").apply { mkdirs() }
        return File(migrationDirectory, "$migrationId.json").also { file ->
            file.outputStream().buffered().use { it.write(archiveBytes) }
        }
    }
}
