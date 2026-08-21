package com.zhelearn.CSUSTPlanet.migration

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

class LegacyMigrationProvider : ContentProvider() {
    private val appContext get() = requireNotNull(context).applicationContext
    private val sessionStore get() = MigrationSessionStore(appContext)

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        MigrationSecurity.enforceTargetCaller(appContext)
        return when (method) {
            MigrationContract.METHOD_INSPECT -> inspect()
            MigrationContract.METHOD_PREPARE -> prepare(requireNotNull(extras))
            MigrationContract.METHOD_ACK -> acknowledge(requireNotNull(extras))
            else -> throw IllegalArgumentException("Unknown migration method")
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        MigrationSecurity.enforceTargetCaller(appContext)
        if (mode != "r") throw FileNotFoundException("Read-only migration provider")
        val migrationId = parseMigrationId(uri)
        val session = sessionStore.requireActive(migrationId)
        if (session.opened) throw FileNotFoundException("Migration archive already opened")
        val archive = session.archivePath?.let(::File)
            ?.takeIf { it.isFile && it.length() in 1..MigrationContract.MAX_ARCHIVE_BYTES.toLong() }
            ?: throw FileNotFoundException("Migration archive unavailable")
        sessionStore.markOpened(migrationId)
        return ParcelFileDescriptor.open(archive, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun inspect(): Bundle = Bundle().apply {
        putInt(MigrationContract.EXTRA_PROTOCOL_VERSION, MigrationContract.PROTOCOL_VERSION)
        putString(MigrationContract.EXTRA_TARGET_PACKAGE, MigrationContract.TARGET_PACKAGE)
        putBoolean("migration_available", true)
    }

    private fun prepare(extras: Bundle): Bundle {
        val migrationId = extras.getString(MigrationContract.EXTRA_MIGRATION_ID).orEmpty()
        val nonce = extras.getString(MigrationContract.EXTRA_NONCE).orEmpty()
        val targetPackage = extras.getString(MigrationContract.EXTRA_TARGET_PACKAGE).orEmpty()
        val protocolVersion = extras.getInt(MigrationContract.EXTRA_PROTOCOL_VERSION, -1)
        if (targetPackage != MigrationContract.TARGET_PACKAGE) throw SecurityException("Invalid migration target")
        require(protocolVersion == MigrationContract.PROTOCOL_VERSION) { "Unsupported migration protocol" }
        val session = sessionStore.requireAuthorized(migrationId, nonce)
        if (session.opened) throw IllegalStateException("Migration session already consumed")
        val archive = session.archivePath?.let(::File)?.takeIf(File::isFile)
            ?: LegacyMigrationExporter(appContext).createSnapshot(migrationId).also {
                sessionStore.attachArchive(migrationId, it)
            }
        return Bundle().apply {
            putString(MigrationContract.EXTRA_MIGRATION_ID, migrationId)
            putParcelable(MigrationContract.EXTRA_CONTENT_URI, MigrationContract.exportUri(migrationId))
            putLong(MigrationContract.EXTRA_EXPIRES_AT, session.expiresAt)
            putLong(MigrationContract.EXTRA_ARCHIVE_SIZE, archive.length())
        }
    }

    private fun acknowledge(extras: Bundle): Bundle {
        val migrationId = extras.getString(MigrationContract.EXTRA_MIGRATION_ID).orEmpty()
        val status = extras.getString(MigrationContract.EXTRA_STATUS).orEmpty()
        if (status !in setOf(MigrationContract.STATUS_SUCCESS, MigrationContract.STATUS_FAILED)) {
            throw IllegalArgumentException("Invalid migration status")
        }
        val session = sessionStore.requireActive(migrationId)
        if (!session.opened) throw IllegalStateException("Migration archive was not opened")
        sessionStore.markReceipt(
            migrationId,
            status,
            extras.getString(MigrationContract.EXTRA_ARCHIVE_DIGEST)
        )
        return Bundle().apply { putString(MigrationContract.EXTRA_STATUS, status) }
    }

    private fun parseMigrationId(uri: Uri): String {
        if (uri.authority != MigrationContract.AUTHORITY || uri.pathSegments.size != 2 ||
            uri.pathSegments[0] != "export"
        ) {
            throw FileNotFoundException("Unknown migration URI")
        }
        return uri.pathSegments[1]
    }

    override fun getType(uri: Uri): String = "application/vnd.com.zhelearn.csustplanet.migration+json"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("Read-only migration provider")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("Read-only migration provider")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = throw UnsupportedOperationException("Read-only migration provider")
}
