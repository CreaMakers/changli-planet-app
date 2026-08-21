package com.creamaker.changli_planet_app.migration

import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest

object MigrationCodec {
    private val gson = Gson()

    fun encode(archive: MigrationArchive): ByteArray = gson.toJson(archive).toByteArray(Charsets.UTF_8)

    /**
     * 归档最大 8MB。这里从字节流直接读，不要先 toString(UTF_8)：
     * 那会额外产生一个约 2 倍大小的 String（Java 字符串按 UTF-16 存），
     * 和字节数组、Gson 解出的对象图同时存活，峰值内存明显抬高。
     */
    fun decode(bytes: ByteArray): MigrationArchive =
        InputStreamReader(ByteArrayInputStream(bytes), Charsets.UTF_8).use { reader ->
            requireNotNull(gson.fromJson(reader, MigrationArchive::class.java))
        }

    /**
     * 边序列化边摘要，不留完整的 JSON 副本。
     *
     * 结果与「先整体序列化再摘要」完全一致（同样的 UTF-8 字节按同样顺序进摘要），
     * 所以不影响已生成归档的校验。
     */
    fun payloadDigest(payload: MigrationPayload): String {
        val digest = MessageDigest.getInstance("SHA-256")
        DigestOutputStream(NullOutputStream, digest).writer(Charsets.UTF_8).use { writer ->
            gson.toJson(payload, writer)
        }
        return digest.digest().toHex()
    }

    fun archiveDigest(bytes: ByteArray): String = sha256(bytes)

    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .toHex()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    /** 只用于驱动 [DigestOutputStream]，写入的字节全部丢弃。 */
    private object NullOutputStream : OutputStream() {
        override fun write(b: Int) = Unit
        override fun write(b: ByteArray, off: Int, len: Int) = Unit
    }
}
