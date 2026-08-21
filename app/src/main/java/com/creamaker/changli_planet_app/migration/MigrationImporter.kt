package com.creamaker.changli_planet_app.migration

import android.content.Context
import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.creamaker.changli_planet_app.feature.common.data.local.room.database.CoursesDataBase
import com.creamaker.changli_planet_app.feature.ledger.data.local.room.database.AccountBookDatabase
import com.creamaker.changli_planet_app.feature.ledger.data.local.room.entity.LedgerItemEntity
import com.creamaker.changli_planet_app.feature.ledger.data.local.room.entity.LedgerTopCardEntity
import com.creamaker.changli_planet_app.skin.data.cache.SkinCache

class MigrationImporter(context: Context) {
    private val context = context.applicationContext
    private val resultStore = context.getSharedPreferences("migration_import_results", Context.MODE_PRIVATE)

    fun import(bytes: ByteArray, expectedMigrationId: String): MigrationImportResult {
        require(bytes.size in 1..MigrationContract.MAX_ARCHIVE_BYTES) { "ARCHIVE_SIZE_INVALID" }
        val archiveDigest = MigrationCodec.archiveDigest(bytes)
        readResult(expectedMigrationId, archiveDigest)?.let { return it }
        val archive = MigrationCodec.decode(bytes)
        MigrationArchiveValidator.validate(archive, expectedMigrationId)
        importPayload(archive.payload)
        verifyPayload(archive.payload)
        val result = MigrationImportResult(
            migrationId = archive.migrationId,
            archiveDigest = archiveDigest,
            courseCount = archive.payload.customCourses.size,
            ledgerCount = archive.payload.ledgerItems.size,
            preferenceCount = archive.payload.preferences.count()
        )
        saveResult(result)
        return result
    }

    private fun importPayload(payload: MigrationPayload) {
        val coursesDatabase = CoursesDataBase.getDatabase(context)
        coursesDatabase.runInTransaction {
            val entities = payload.customCourses.map { course ->
                TimeTableMySubject(
                    courseName = course.courseName,
                    classroom = course.classroom,
                    teacher = course.teacher,
                    weeks = course.weeks,
                    start = course.start,
                    step = course.step,
                    weekday = course.weekday,
                    isCustom = true,
                    term = course.term,
                    studentId = course.studentId,
                    studentPassword = ""
                )
            }.toMutableList()
            coursesDatabase.courseDao().insertCourses(entities)
        }

        val ledgerDatabase = AccountBookDatabase.getInstance(context)
        ledgerDatabase.runInTransaction {
            val dao = ledgerDatabase.accountBookDao()
            payload.ledgerItems.forEach { item ->
                val existingItem = dao.findItemById(item.sourceId)
                if (existingItem == null) {
                    dao.insertSomethingItemIfAbsent(
                        LedgerItemEntity(
                            id = item.sourceId,
                            name = item.name,
                            totalMoney = item.totalMoney,
                            dailyAverage = item.dailyAverage,
                            startTime = item.startTime,
                            picture = item.picture,
                            username = item.username
                        )
                    )
                } else {
                    require(ledgerKey(existingItem) == ledgerKey(item)) { "LEDGER_ID_CONFLICT" }
                }
            }
            payload.ledgerItems.map { it.username }.distinct().forEach { username ->
                val items = dao.getSomethingItemsByUsername(username)
                dao.insertOrUpdateTopCard(
                    LedgerTopCardEntity(
                        username = username,
                        allNumber = items.size,
                        totalMoney = items.sumOf { it.totalMoney },
                        dailyAverage = items.sumOf { it.dailyAverage }
                    )
                )
            }
        }

        SkinCache.saveAssetsName(payload.preferences.skinName)
        SkinCache.saveIsUsingSkin(payload.preferences.activeSkinName)
    }

    private fun verifyPayload(payload: MigrationPayload) {
        val importedCourseKeys = CoursesDataBase.getDatabase(context).courseDao()
            .getAllCustomCourses()
            .map { courseKey(it) }
            .toSet()
        require(payload.customCourses.all { courseKey(it) in importedCourseKeys }) { "COURSE_VERIFY_FAILED" }

        val importedLedgerKeys = AccountBookDatabase.getInstance(context).accountBookDao()
            .getAllSomethingItems()
            .map { ledgerKey(it) }
            .toSet()
        require(payload.ledgerItems.all { ledgerKey(it) in importedLedgerKeys }) { "LEDGER_VERIFY_FAILED" }
        require(SkinCache.getAssetsName() == payload.preferences.skinName) { "PREFERENCES_VERIFY_FAILED" }
        require(SkinCache.getIsUsingSkin() == payload.preferences.activeSkinName) { "PREFERENCES_VERIFY_FAILED" }
    }

    private fun courseKey(course: MigrationCourse): String = listOf(
        course.courseName, course.classroom, course.teacher, course.weeks.sorted().joinToString(","),
        course.start, course.step, course.weekday, course.term, course.studentId
    ).joinToString("\u0000")

    private fun courseKey(course: TimeTableMySubject): String = listOf(
        course.courseName, course.classroom.orEmpty(), course.teacher, course.weeks.orEmpty().sorted().joinToString(","),
        course.start, course.step, course.weekday, course.term, course.studentId
    ).joinToString("\u0000")

    private fun ledgerKey(item: MigrationLedgerItem): String = listOf(
        item.sourceId, item.username, item.name, item.totalMoney.toBits(), item.dailyAverage.toBits(),
        item.startTime, item.picture
    ).joinToString("\u0000")

    private fun ledgerKey(item: LedgerItemEntity): String = listOf(
        item.id, item.username, item.name, item.totalMoney.toBits(), item.dailyAverage.toBits(),
        item.startTime, item.picture
    ).joinToString("\u0000")

    /**
     * 写入本次迁移的幂等记录，同时清掉其他 migrationId 的历史记录。
     *
     * 每次迁移都会以新的 migrationId 写 4 个 key，之前从不清理。SharedPreferences 由
     * ContextImpl 按文件名静态缓存，整个 map 在进程存活期间常驻内存，条目只增不减，
     * 反复迁移就成了持续增长且不会回收的内存占用。
     * 幂等校验只需要判断「当前这份归档是否已经导入过」，历史 id 没有保留价值。
     */
    private fun saveResult(result: MigrationImportResult) {
        val editor = resultStore.edit()
        val currentPrefix = "${result.migrationId}:"
        resultStore.all.keys
            .filterNot { it.startsWith(currentPrefix) }
            .forEach(editor::remove)
        editor
            .putString(key(result.migrationId, "digest"), result.archiveDigest)
            .putInt(key(result.migrationId, "courses"), result.courseCount)
            .putInt(key(result.migrationId, "ledger"), result.ledgerCount)
            .putInt(key(result.migrationId, "preferences"), result.preferenceCount)
            .apply()
    }

    private fun readResult(migrationId: String, archiveDigest: String): MigrationImportResult? {
        val savedDigest = resultStore.getString(key(migrationId, "digest"), null) ?: return null
        require(savedDigest == archiveDigest) { "MIGRATION_ID_REUSED" }
        return MigrationImportResult(
            migrationId = migrationId,
            archiveDigest = savedDigest,
            courseCount = resultStore.getInt(key(migrationId, "courses"), 0),
            ledgerCount = resultStore.getInt(key(migrationId, "ledger"), 0),
            preferenceCount = resultStore.getInt(key(migrationId, "preferences"), 0)
        )
    }

    private fun key(migrationId: String, field: String) = "$migrationId:$field"
}
