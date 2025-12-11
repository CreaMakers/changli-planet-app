package com.dcelysia.csust_spider.core

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    private fun randomString(length: Int): String {
        val letters = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678"
        return (1..length)
            .map { letters.random() }
            .joinToString("")
    }

    private fun aesEncrypt(data: String, key: String, iv: String): String? {
        return try {
            val keyBytes = key.toByteArray().take(16).toByteArray()
            val ivBytes = iv.toByteArray().take(16).toByteArray()

            val secretKeySpec = SecretKeySpec(keyBytes, "AES")
            val ivParameterSpec = IvParameterSpec(ivBytes)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

            val encrypted = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun encryptPassword(password: String, salt: String?): String {
        if (salt.isNullOrEmpty()) {
            return password
        }

        val randomSalt = randomString(64)
        val iv = randomString(16)
        val combinedData = randomSalt + password

        return aesEncrypt(combinedData, salt, iv) ?: password
    }
}