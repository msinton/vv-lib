package com.consideredgames.security

import org.jasypt.util.password.StrongPasswordEncryptor

object EncryptionUtils {

  private val passwordEncryptor: StrongPasswordEncryptor = new StrongPasswordEncryptor

  def encrypt(str: String): String = passwordEncryptor.encryptPassword(str)

  def isPasswordMatch(plainPwd: String, encryptedPwd: String): Boolean = {
    passwordEncryptor.checkPassword(plainPwd, encryptedPwd)
  }
}