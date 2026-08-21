package com.zhelearn.CSUSTPlanet.migration

import android.content.Context
import android.util.Base64
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

data class MigrationSession(
    val migrationId: String,
    val nonce: String,
    val expiresAt: Long,
    val archivePath: String?,
    val opened: Boolean
)

class MigrationSessionStore(context: Context) {
    private val context = context.applicationContext
    private val preferences = context.getSharedPreferences("migration_sessions", Context.MODE_PRIVATE)

    fun create(): MigrationSession {
        cleanup()
        val migrationId = UUID.randomUUID().toString()
        val randomBytes = ByteArray(32).also(SecureRandom()::nextBytes)
        val nonce = Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE)
        val expiresAt = System.currentTimeMillis() + MigrationContract.SESSION_TTL_MILLIS
        preferences.edit()
            .putString(key(migrationId, "nonce"), nonce)
            .putLong(key(migrationId, "expires"), expiresAt)
            .putBoolean(key(migrationId, "opened"), false)
            .apply()
        return MigrationSession(migrationId, nonce, expiresAt, null, false)
    }

    fun requireAuthorized(migrationId: String, nonce: String): MigrationSession {
        val session = requireActive(migrationId)
        val validNonce = MessageDigest.isEqual(
            session.nonce.toByteArray(Charsets.UTF_8),
            nonce.toByteArray(Charsets.UTF_8)
        )
        if (!validNonce) throw SecurityException("Invalid migration session")
        return session
    }

    fun requireActive(migrationId: String): MigrationSession {
        if (!UUID_PATTERN.matches(migrationId)) throw SecurityException("Invalid migration session")
        val nonce = preferences.getString(key(migrationId, "nonce"), null)
            ?: throw SecurityException("Unknown migration session")
        val expiresAt = preferences.getLong(key(migrationId, "expires"), 0L)
        if (expiresAt < System.currentTimeMillis()) {
            remove(migrationId)
            throw SecurityException("Expired migration session")
        }
        return MigrationSession(
            migrationId,
            nonce,
            expiresAt,
            preferences.getString(key(migrationId, "archive"), null),
            preferences.getBoolean(key(migrationId, "opened"), false)
        )
    }

    fun attachArchive(migrationId: String, archive: File) {
        preferences.edit().putString(key(migrationId, "archive"), archive.absolutePath).apply()
    }

    fun markOpened(migrationId: String) {
        preferences.edit().putBoolean(key(migrationId, "opened"), true).apply()
    }

    fun markReceipt(migrationId: String, status: String, digest: String?) {
        preferences.edit()
            .putString(key(migrationId, "status"), status)
            .putString(key(migrationId, "digest"), digest)
            .apply()
        if (status == MigrationContract.STATUS_SUCCESS) {
            archiveFile(migrationId)?.delete()
        }
    }

    fun receiptStatus(migrationId: String): String? =
        preferences.getString(key(migrationId, "status"), null)

    private fun archiveFile(migrationId: String): File? =
        preferences.getString(key(migrationId, "archive"), null)?.let(::File)

    private fun cleanup() {
        val expiredIds = preferences.all.keys
            .mapNotNull { it.substringBefore(':').takeIf(UUID_PATTERN::matches) }
            .distinct()
            .filter { preferences.getLong(key(it, "expires"), 0L) < System.currentTimeMillis() }
        expiredIds.forEach(::remove)
    }

    private fun remove(migrationId: String) {
        archiveFile(migrationId)?.delete()
        val editor = preferences.edit()
        preferences.all.keys.filter { it.startsWith("$migrationId:") }.forEach(editor::remove)
        editor.apply()
    }

    private fun key(migrationId: String, field: String) = "$migrationId:$field"

    private companion object {
        val UUID_PATTERN = Regex("[0-9a-fA-F-]{36}")
    }
}
