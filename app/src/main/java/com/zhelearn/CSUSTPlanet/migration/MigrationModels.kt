package com.zhelearn.CSUSTPlanet.migration

data class MigrationArchive(
    val format: String = "changli-planet-migration",
    val protocolVersion: Int = MigrationContract.PROTOCOL_VERSION,
    val migrationId: String,
    val sourcePackage: String = MigrationContract.SOURCE_PACKAGE,
    val targetPackage: String = MigrationContract.TARGET_PACKAGE,
    val sourceVersionCode: Int,
    val createdAt: Long,
    val payloadDigest: String,
    val payload: MigrationPayload
)

data class MigrationPayload(
    val customCourses: List<MigrationCourse>,
    val ledgerItems: List<MigrationLedgerItem>,
    val preferences: MigrationPreferences
)

data class MigrationCourse(
    val courseName: String,
    val classroom: String,
    val teacher: String,
    val weeks: List<Int>,
    val start: Int,
    val step: Int,
    val weekday: Int,
    val term: String,
    val studentId: String
)

data class MigrationLedgerItem(
    val sourceId: Int,
    val name: String,
    val totalMoney: Double,
    val dailyAverage: Double,
    val startTime: String,
    val picture: Int,
    val username: String
)

data class MigrationPreferences(
    val skinName: String,
    val activeSkinName: String
) {
    fun count(): Int = listOf(skinName, activeSkinName).count { it.isNotBlank() }
}

data class MigrationImportResult(
    val migrationId: String,
    val archiveDigest: String,
    val courseCount: Int,
    val ledgerCount: Int,
    val preferenceCount: Int
)
