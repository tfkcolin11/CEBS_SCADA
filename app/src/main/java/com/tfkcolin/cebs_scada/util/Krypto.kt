package com.tfkcolin.cebs_scada.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.Throws

// \uFDD0, \uFDEF, \uFFFE, \uFFFF are UNICODE character that are never used so can be use as end of string
val iv = IvParameterSpec(
    byteArrayOf(
        0x1f, 0x10, 0x2f, 0x0e,
        0x1f, 0x37, 0x1f, 0x1f,
        0x10, 0x1f, 0x14, 0x52,
        0x1a, 0x1f, 0x1f, 0x1f
    )
)

val key = SecretKeySpec(
    byteArrayOf(
        0x30, 0x30, 0x30, 0x30,
        0x30, 0x30, 0x30, 0x30,
        0x30, 0x30, 0x30, 0x30,
        0x30, 0x30, 0x30, 0x30
    ),
    "AES"
)

private const val EOS = 3.toByte()

/**
 * generate a secretKey of the given length
 * @param keyLength should be 128, 192, 256
 */
@Throws(NoSuchAlgorithmException::class)
fun generateAESSecretKey(keyLength: Int): SecretKey{
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(keyLength)
    return keyGenerator.generateKey()
}

/**
 * generate a secretKey from the given params
 * @param pwd the password used to generate the key
 * @param salt will be use to turn the pwd into a secret key. it is a random value
 * @param keyLength the length of the key: should be 128, 192, 256
 */
@Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
fun getAESKeyFromPassword(pwd: String, salt: String, keyLength: Int): SecretKey {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec = PBEKeySpec(pwd.toCharArray(), salt.toByteArray(), 65536, keyLength)
    return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
}

fun generateAESIv(): IvParameterSpec {
    val iv = ByteArray(16)
    SecureRandom().nextBytes(iv)
    return IvParameterSpec(iv)
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun encrypt(algorithm: String, input: String, key: SecretKey, iv: IvParameterSpec): String {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)
    var bytes = input.toByteArray()
    while(bytes.size % 16 != 0){
        bytes += EOS
    }
    val cipherText = cipher.doFinal(bytes)
    return android.util.Base64.encodeToString(cipherText, android.util.Base64.DEFAULT)
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun encrypt(algorithm: String, input: String, key: SecretKeySpec, iv: IvParameterSpec): String {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)
    var bytes = input.toByteArray()
    while(bytes.size % 16 != 0){
        bytes += EOS
    }
    val cipherText = cipher.doFinal(bytes)
    return android.util.Base64.encodeToString(cipherText, android.util.Base64.DEFAULT)
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun decrypt(algorithm: String, cipherText: String, key: SecretKey, iv: IvParameterSpec): String {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, key, iv)
    val plainText = cipher.doFinal(android.util.Base64.decode(cipherText, android.util.Base64.DEFAULT))
        .filter { b -> b != EOS }
        .toByteArray()
    return String(plainText)
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun decrypt(algorithm: String, cipherText: String, key: SecretKeySpec, iv: IvParameterSpec): String {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, key, iv)
    val plainText = cipher.doFinal(android.util.Base64.decode(cipherText, android.util.Base64.DEFAULT))
        .filter { b -> b != EOS }
        .toByteArray()
    return String(plainText)
}


@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun encryptToByte(algorithm: String, input: String, key: SecretKeySpec, iv: IvParameterSpec): ByteArray {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)
    var bytes = input.toByteArray()
    while (bytes.size % 16 != 0) {
        bytes += EOS
    }
    return cipher.doFinal(bytes)
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun decryptByte(algorithm: String, cipherText: ByteArray, key: SecretKey, iv: IvParameterSpec): String {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, key, iv)
    val plainText = cipher.doFinal(cipherText)
        .filter { b -> b != EOS }
        .toByteArray()
    return String(plainText)
}

fun ByteArray.toHexString(): String {
    return String.format("%0${size * 2}x", BigInteger(1, this))
}

fun String.hexStringToByteArray(): ByteArray? {
    val out = ByteArray(length / 2)
    for(i in out.indices){
        try {
            out[i] = Integer.parseInt(this.substring(2 * i, 2 * i + 2), 16).toByte()
        }catch (e:  NumberFormatException){
            return null
        }
    }
    return out
}