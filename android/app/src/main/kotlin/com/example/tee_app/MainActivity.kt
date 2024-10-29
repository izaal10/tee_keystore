package com.example.tee_app

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import javax.crypto.Cipher

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.app/keystore"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "generateKeyPair" -> {
                    result.success(generateKeyPair())
                }
                "encryptText" -> {
                    val text = call.argument<String>("text") ?: ""
                    result.success(encryptText(text))
                }
                "decryptText" -> {
                    val encryptedText = call.argument<String>("encryptedText") ?: ""
                    result.success(decryptText(encryptedText))
                }
                else -> result.notImplemented()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateKeyPair(): String {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder("alias_key", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()
        )
        keyPairGenerator.generateKeyPair()
        return "Keypair generated"
    }

    private fun encryptText(text: String): String {
        val publicKey = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.getCertificate("alias_key").publicKey
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply { init(Cipher.ENCRYPT_MODE, publicKey) }
        val encryptedBytes = cipher.doFinal(text.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decryptText(encryptedText: String): String {
        val privateKey = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.getKey("alias_key", null) as PrivateKey
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply { init(Cipher.DECRYPT_MODE, privateKey) }
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT))
        return String(decryptedBytes)
    }
}
