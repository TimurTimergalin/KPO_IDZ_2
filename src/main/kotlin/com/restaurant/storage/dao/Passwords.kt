package com.restaurant.storage.dao

import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

object Passwords {
    private val random = SecureRandom()

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    private fun concatenateByteArrays(b1: ByteArray, b2: ByteArray): ByteArray = ByteArrayOutputStream().run {
        write(b1)
        write(b2)
        toByteArray()
    }

    fun generatePassword(password: String): String {
        val salt = generateSalt()
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val toHash = concatenateByteArrays(salt, passwordBytes)

        val hash = MessageDigest.getInstance("SHA-256")
        with(Base64.getEncoder()) {
            return encodeToString(salt) + "$" + encodeToString(hash.digest(toHash))
        }
    }

    fun checkPassword(passwordRecord: String, password: String): Boolean {
        with(Base64.getDecoder())
        {
            val (salt, realPasswordHash) = passwordRecord.split('$').map { decode(it) }
            val toHash = concatenateByteArrays(salt, password.toByteArray(Charsets.UTF_8))
            return realPasswordHash.contentEquals(MessageDigest.getInstance("SHA-256").digest(toHash))
        }
    }
}