package com.consideredgames.security

import org.scalatest.FunSuite

/**
  * Created by matt on 19/04/17.
  */
class EncryptionUtilsTest extends FunSuite {

  test("matching passwords") {

    val encrypted_password = "29XxCKUHU6yaBxjrD4pb3ICFB1cSFGGLg+Fw6krRk07YbQMPZQtm5yIpuLG0HRJz"
    val encrypted_password_2 = "I+X3NrbI6FKHK+7cnoET0IsQR8CwCs9EDgSOeojBvm7Jzl2DzA/J47crDFhzBoiV"

    val encrypted_password_bad = "I+X3NrbI6FKHK+7cnoET0IsQR8CwCs9EDgSOeojBvm7Jzl2DzA/J47crDFhzBoiR" //last char different

    assert(EncryptionUtils.isPasswordMatch("password", encrypted_password))
    assert(EncryptionUtils.isPasswordMatch("password", encrypted_password_2))

    assert(!EncryptionUtils.isPasswordMatch("password", encrypted_password_bad))
    assert(!EncryptionUtils.isPasswordMatch("password1", encrypted_password))
  }

  test("double encrypt") {

    val pswrd = "abc123dfg"
    val encryptedOnce = EncryptionUtils.encrypt(pswrd)
    val encryptedTwice = EncryptionUtils.encrypt(encryptedOnce)

    assert(EncryptionUtils.isPasswordMatch(encryptedOnce, encryptedTwice))
  }

}
