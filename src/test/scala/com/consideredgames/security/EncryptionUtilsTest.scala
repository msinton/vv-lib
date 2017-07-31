package com.consideredgames.security

import org.scalatest.FunSuite

/**
  * Created by matt on 19/04/17.
  */
class EncryptionUtilsTest extends FunSuite {

  test("hash and validate password") {
    val password = "password"
    val salt = EncryptionUtils.generateSalt()
    val hash = EncryptionUtils.hashPassword(password, salt)
    assert(EncryptionUtils.validatePassword(password, hash))
    assert(!EncryptionUtils.validatePassword("password2", hash))
    assert(!EncryptionUtils.validatePassword("password", EncryptionUtils.hashPassword("pass", salt)))
  }

  test("simulate register then login. send one-way hash, then re-hash with salt. " +
    "Next login send new hash") {
    val password = "password"
    val username = "username"
    // This is a temporary solution. Not adequately secure.
    // client-side: Register (optionally store)
    val hash = EncryptionUtils.hash(password, username.getBytes)

    // server-side:
    val salt = EncryptionUtils.generateSalt()
    val rehash = EncryptionUtils.hashPassword(hash, salt)

    assert(EncryptionUtils.validatePassword(hash, rehash))
    assert(!EncryptionUtils.validatePassword(password, rehash))

    // client-side:
    val loginHash = EncryptionUtils.hash(password, username.getBytes)
    assert(EncryptionUtils.validatePassword(loginHash, rehash))
  }

}
