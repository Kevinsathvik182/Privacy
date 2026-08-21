package com.example

import com.example.crypto.AES256GCM
import com.example.crypto.HalfNumberCipher
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testHalfNumberCipher_HI() {
    val input = "HI"
    val cipher = HalfNumberCipher.encrypt(input)
    assertEquals("4.0 4.5", cipher)

    val decrypted = HalfNumberCipher.decrypt(cipher)
    assertEquals("HI", decrypted)
  }

  @Test
  fun testHalfNumberCipher_Sentence() {
    val input = "SECRET MESH"
    val cipher = HalfNumberCipher.encrypt(input)
    val decrypted = HalfNumberCipher.decrypt(cipher)
    assertEquals(input, decrypted)
  }

  @Test
  fun testAES256GCM_EncryptionDecryption() {
    val key = AES256GCM.generateKey()
    val plain = "CONFIDENTIAL OFFLINE MESSAGE"
    val encrypted = AES256GCM.encrypt(plain, key)
    assertNotEquals(plain, encrypted)

    val decrypted = AES256GCM.decrypt(encrypted, key)
    assertEquals(plain, decrypted)
  }
}
