package jp.sabiz.android.clipsecret.util

import java.security.KeyStore
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.KeyPairGenerator
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import java.security.PrivateKey
import javax.crypto.CipherInputStream


/**
 * @auther sabi
 * Created on 2017/11/12.
 */


val KEY_STORE_PROVIDER = "AndroidKeyStore"
val ALGORITHM = "RSA/ECB/OAEPWithSHA-512AndMGF1Padding"

fun loadKeyStore():KeyStore {
    val ks = KeyStore.getInstance(KEY_STORE_PROVIDER)
    ks.load(null)
    return ks
}

fun containsKey(keyStore: KeyStore,alias: String):Boolean {
    return keyStore.containsAlias(alias)
}

fun addKeyIfNeed(keyStore: KeyStore,alias: String){
    if(containsKey(keyStore, alias)){
        return
    }

    val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_PROVIDER)
    keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .build())
    keyPairGenerator.generateKeyPair()
}

fun encrypt(keyStore: KeyStore,alias: String, plainText: String):String{
    val publicKey = keyStore.getCertificate(alias).publicKey
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val outputStream = ByteArrayOutputStream()
    val cipherOutputStream = CipherOutputStream(outputStream, cipher)
    cipherOutputStream.write(plainText.toByteArray(Charset.defaultCharset()))
    cipherOutputStream.close()
    val bytes = outputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun decrypt(keyStore: KeyStore,alias: String, encryptedText: String):String{
    val privateKey = keyStore.getKey(alias, null) as PrivateKey
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    val cipherInputStream = CipherInputStream(ByteArrayInputStream(Base64.decode(encryptedText, Base64.DEFAULT)), cipher)
    val outputStream = ByteArrayOutputStream()
    outputStream.write(cipherInputStream.readBytes())
    outputStream.close()
    return outputStream.toString(Charset.defaultCharset().displayName())

}