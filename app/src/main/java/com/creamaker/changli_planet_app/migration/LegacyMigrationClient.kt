package com.creamaker.changli_planet_app.migration

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.CancellationException

class LegacyMigrationClient(context: Context) {
    private val context = context.applicationContext
    fun import(migrationId: String, nonce: String): MigrationImportResult {
        require(context.packageName == MigrationContract.TARGET_PACKAGE) { "TARGET_PACKAGE_MISMATCH" }
        require(MigrationSecurity.isInstalledWithMatchingSignature(context, MigrationContract.SOURCE_PACKAGE)) {
            "SIGNATURE_MISMATCH"
        }
        inspect()
        val response = context.contentResolver.call(
            BASE_URI,
            MigrationContract.METHOD_PREPARE,
            null,
            Bundle().apply {
                putString(MigrationContract.EXTRA_MIGRATION_ID, migrationId)
                putString(MigrationContract.EXTRA_NONCE, nonce)
                putString(MigrationContract.EXTRA_TARGET_PACKAGE, MigrationContract.TARGET_PACKAGE)
                putInt(MigrationContract.EXTRA_PROTOCOL_VERSION, MigrationContract.PROTOCOL_VERSION)
            }
        ) ?: error("PREPARE_FAILED")
        val archiveSize = response.getLong(MigrationContract.EXTRA_ARCHIVE_SIZE, -1L)
        require(archiveSize in 1..MigrationContract.MAX_ARCHIVE_BYTES.toLong()) { "ARCHIVE_SIZE_INVALID" }
        val contentUri = response.contentUri()
        val bytes = context.contentResolver.openInputStream(contentUri)?.use { input ->
            readExactly(input, archiveSize.toInt())
        } ?: error("ARCHIVE_OPEN_FAILED")
        return try {
            MigrationImporter(context).import(bytes, migrationId).also { result ->
                runCatching { acknowledge(result) }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            acknowledgeFailure(migrationId, errorCode(error))
            throw error
        }
    }

    private fun inspect() {
        val response = context.contentResolver.call(
            BASE_URI,
            MigrationContract.METHOD_INSPECT,
            null,
            null
        ) ?: error("INSPECT_FAILED")
        require(response.getInt(MigrationContract.EXTRA_PROTOCOL_VERSION, -1) == MigrationContract.PROTOCOL_VERSION) {
            "UNSUPPORTED_PROTOCOL"
        }
        require(response.getString(MigrationContract.EXTRA_TARGET_PACKAGE) == MigrationContract.TARGET_PACKAGE) {
            "TARGET_PACKAGE_MISMATCH"
        }
    }

    private fun acknowledge(result: MigrationImportResult) {
        context.contentResolver.call(
            BASE_URI,
            MigrationContract.METHOD_ACK,
            null,
            Bundle().apply {
                putString(MigrationContract.EXTRA_MIGRATION_ID, result.migrationId)
                putString(MigrationContract.EXTRA_STATUS, MigrationContract.STATUS_SUCCESS)
                putString(MigrationContract.EXTRA_ARCHIVE_DIGEST, result.archiveDigest)
                putInt(MigrationContract.EXTRA_COURSE_COUNT, result.courseCount)
                putInt(MigrationContract.EXTRA_LEDGER_COUNT, result.ledgerCount)
                putInt(MigrationContract.EXTRA_PREFERENCE_COUNT, result.preferenceCount)
            }
        )
    }

    private fun acknowledgeFailure(migrationId: String, errorCode: String) {
        runCatching {
            context.contentResolver.call(
                BASE_URI,
                MigrationContract.METHOD_ACK,
                null,
                Bundle().apply {
                    putString(MigrationContract.EXTRA_MIGRATION_ID, migrationId)
                    putString(MigrationContract.EXTRA_STATUS, MigrationContract.STATUS_FAILED)
                    putString(MigrationContract.EXTRA_ERROR_CODE, errorCode)
                }
            )
        }
    }

    private fun Bundle.contentUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireNotNull(getParcelable(MigrationContract.EXTRA_CONTENT_URI, Uri::class.java))
        } else {
            @Suppress("DEPRECATION")
            requireNotNull(getParcelable(MigrationContract.EXTRA_CONTENT_URI))
        }
    }

    /**
     * 按 prepare 声明的大小一次性分配缓冲区读满归档。
     *
     * 不用 ByteArrayOutputStream：它在增长时会反复扩容并在 toByteArray() 再整体拷贝一份，
     * 8MB 的归档峰值会放大到 3 倍左右。这里 size 已由 ARCHIVE_SIZE_INVALID 校验过。
     */
    private fun readExactly(input: java.io.InputStream, size: Int): ByteArray {
        val bytes = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val count = input.read(bytes, offset, size - offset)
            if (count < 0) break
            offset += count
        }
        require(offset == size) { "ARCHIVE_SIZE_MISMATCH" }
        require(input.read() < 0) { "ARCHIVE_SIZE_MISMATCH" }
        return bytes
    }

    private fun errorCode(throwable: Throwable): String = throwable.message
        ?.takeIf { it.matches(Regex("[A-Z_]{3,64}")) }
        ?: "IMPORT_FAILED"

    private companion object {
        val BASE_URI: Uri = Uri.parse("content://${MigrationContract.AUTHORITY}")
    }
}
