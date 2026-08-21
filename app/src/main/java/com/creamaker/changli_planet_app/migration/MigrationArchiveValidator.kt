package com.creamaker.changli_planet_app.migration

object MigrationArchiveValidator {
    fun validate(archive: MigrationArchive, expectedMigrationId: String, now: Long = System.currentTimeMillis()) {
        require(archive.format == "changli-planet-migration") { "ARCHIVE_FORMAT_INVALID" }
        require(archive.protocolVersion == MigrationContract.PROTOCOL_VERSION) { "UNSUPPORTED_PROTOCOL" }
        require(archive.migrationId == expectedMigrationId) { "MIGRATION_ID_MISMATCH" }
        require(archive.sourcePackage == MigrationContract.SOURCE_PACKAGE) { "SOURCE_PACKAGE_MISMATCH" }
        require(archive.targetPackage == MigrationContract.TARGET_PACKAGE) { "TARGET_PACKAGE_MISMATCH" }
        require(archive.createdAt in (now - MigrationContract.SESSION_TTL_MILLIS)..now) { "ARCHIVE_EXPIRED" }
        require(archive.payloadDigest == MigrationCodec.payloadDigest(archive.payload)) { "ARCHIVE_CORRUPTED" }
        require(archive.payload.customCourses.size <= 5_000) { "COURSE_LIMIT_EXCEEDED" }
        require(archive.payload.ledgerItems.size <= 20_000) { "LEDGER_LIMIT_EXCEEDED" }
        archive.payload.customCourses.forEach(::validateCourse)
        archive.payload.ledgerItems.forEach(::validateLedgerItem)
        validatePreferences(archive.payload.preferences)
    }

    private fun validateCourse(course: MigrationCourse) {
        require(course.courseName.isNotBlank() && course.courseName.length <= 200) { "COURSE_INVALID" }
        require(course.classroom.length <= 200 && course.teacher.length <= 200) { "COURSE_INVALID" }
        require(course.term.isNotBlank() && course.term.length <= 100) { "COURSE_INVALID" }
        require(course.studentId.length <= 100) { "COURSE_INVALID" }
        require(course.weekday in 0..7 && course.start in 0..24 && course.step in 1..24) { "COURSE_INVALID" }
        require(course.weeks.size <= 60 && course.weeks.all { it in 0..60 }) { "COURSE_INVALID" }
    }

    private fun validateLedgerItem(item: MigrationLedgerItem) {
        require(item.sourceId > 0) { "LEDGER_INVALID" }
        require(item.name.isNotBlank() && item.name.length <= 200) { "LEDGER_INVALID" }
        require(item.username.length <= 200 && item.startTime.length <= 100) { "LEDGER_INVALID" }
        require(item.totalMoney.isFinite() && item.dailyAverage.isFinite()) { "LEDGER_INVALID" }
        require(item.totalMoney >= 0.0 && item.dailyAverage >= 0.0) { "LEDGER_INVALID" }
    }

    private fun validatePreferences(preferences: MigrationPreferences) {
        val allowedSkins = setOf("skin_default", "skin_dark.apk")
        require(preferences.skinName in allowedSkins && preferences.activeSkinName in allowedSkins) {
            "PREFERENCES_INVALID"
        }
    }
}
