package com.creamaker.changli_planet_app.migration

import android.net.Uri

object MigrationContract {
    const val SOURCE_PACKAGE = "com.example.changli_planet_app"
    const val TARGET_PACKAGE = "com.zhelearn.CSUSTPlanet"
    const val AUTHORITY = "com.example.changli_planet_app.migration"
    const val PERMISSION = "com.zhelearn.CSUSTPlanet.permission.MIGRATE_DATA"
    const val ACTION_DATA_MIGRATION = "com.zhelearn.CSUSTPlanet.action.DATA_MIGRATION"
    const val PROTOCOL_VERSION = 1
    const val MAX_ARCHIVE_BYTES = 8 * 1024 * 1024
    const val SESSION_TTL_MILLIS = 10 * 60 * 1000L

    const val METHOD_INSPECT = "inspect"
    const val METHOD_PREPARE = "prepare"
    const val METHOD_ACK = "ack"

    const val EXTRA_MIGRATION_ID = "migration_id"
    const val EXTRA_NONCE = "nonce"
    const val EXTRA_TARGET_PACKAGE = "target_package"
    const val EXTRA_PROTOCOL_VERSION = "protocol_version"
    const val EXTRA_CONTENT_URI = "content_uri"
    const val EXTRA_EXPIRES_AT = "expires_at"
    const val EXTRA_ARCHIVE_SIZE = "archive_size"
    const val EXTRA_STATUS = "status"
    const val EXTRA_ARCHIVE_DIGEST = "archive_digest"
    const val EXTRA_COURSE_COUNT = "course_count"
    const val EXTRA_LEDGER_COUNT = "ledger_count"
    const val EXTRA_PREFERENCE_COUNT = "preference_count"
    const val EXTRA_ERROR_CODE = "error_code"

    const val STATUS_SUCCESS = "SUCCESS"
    const val STATUS_FAILED = "FAILED"

    fun exportUri(migrationId: String): Uri =
        Uri.parse("content://$AUTHORITY/export/$migrationId")
}
