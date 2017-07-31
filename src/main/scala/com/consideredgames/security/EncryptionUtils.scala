package com.consideredgames.security

import java.security.{MessageDigest, SecureRandom}
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import base64.Decode.{urlSafe => fromBase64UrlSafe}
import base64.Encode.{urlSafe => toBase64UrlSafe}

object EncryptionUtils {

  private val sizeOfPasswordHash = 32
  private val nrOfIterations = 2000
  private val random = new SecureRandom()
  private val separator = ":"

  private def pbkdf2(password: String, salt: Array[Byte], nrOfIterations: Int): Array[Byte] = {
    val keySpec = new PBEKeySpec(password.toCharArray, salt, nrOfIterations, sizeOfPasswordHash * 8)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    keyFactory.generateSecret(keySpec).getEncoded
  }

  def generateSalt(): Array[Byte] = {
    val salt: Array[Byte] = Array[Byte](16)
    random.nextBytes(salt)
    salt
  }

  def hash(str: String, salt: Array[Byte]): String = {
    val md = MessageDigest.getInstance("SHA-512")
    md.update(salt)
    val hash = md.digest(str.getBytes)
    new String(toBase64UrlSafe(hash))
  }

  def hashPassword(password: String, salt: Array[Byte]): String = {
    val hash = pbkdf2(password, salt, nrOfIterations)
    val salt64 = new String(toBase64UrlSafe(salt))
    val hash64 = new String(toBase64UrlSafe(hash))
    s"$nrOfIterations$separator$hash64$separator$salt64"
  }

  def validatePassword(password: String, hashedPassword: String): Boolean = {
    /** Compares two byte arrays in length-constant time to prevent timing attacks. */
    def slowEquals(a: Array[Byte], b: Array[Byte]): Boolean = {
      var diff = a.length ^ b.length
      for (i <- 0 until math.min(a.length, b.length)) diff |= a(i) ^ b(i)
      diff == 0
    }

    val hashParts = hashedPassword.split(separator)

    if (hashParts.length != 3) return false
    if (!hashParts(0).forall(_.isDigit)) return false

    val nrOfIterations = hashParts(0).toInt
    val hash = fromBase64UrlSafe(hashParts(1))
    val salt = fromBase64UrlSafe(hashParts(2))

    if (hash.isLeft || salt.isLeft) return false
    if (hash.right.get.length == 0 || salt.right.get.length == 0) return false

    val calculatedHash = pbkdf2(password, salt.right.get, nrOfIterations)

    slowEquals(calculatedHash, hash.right.get)
  }
}