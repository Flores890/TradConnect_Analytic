package com.example.tradconnect_analytic.datos

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/** Protege la sesión con una clave no exportable del Android Keystore. */
class TokenManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(SECURE_PREFS, Context.MODE_PRIVATE)
    private val legacyPrefs: SharedPreferences =
        appContext.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)

    init {
        migrateLegacySession()
    }

    @Synchronized
    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        role: String,
        userName: String? = null
    ) {
        val session = JSONObject()
            .put("access", accessToken)
            .put("refresh", refreshToken)
            .put("role", role)
            .put("userName", userName.orEmpty())
        prefs.edit().putString(SESSION_KEY, encrypt(session.toString())).apply()
        legacyPrefs.edit().clear().apply()
    }

    @Synchronized
    fun getToken(): String? = readSession()?.optString("access")?.takeIf { it.isNotBlank() }

    @Synchronized
    fun getRefreshToken(): String? = readSession()?.optString("refresh")?.takeIf { it.isNotBlank() }

    @Synchronized
    fun getRole(): String? = readSession()?.optString("role")?.takeIf { it.isNotBlank() }

    @Synchronized
    fun getUserName(): String? = readSession()?.optString("userName")?.takeIf { it.isNotBlank() }

    @Synchronized
    fun updateAccessToken(accessToken: String) {
        val session = readSession() ?: return
        session.put("access", accessToken)
        prefs.edit().putString(SESSION_KEY, encrypt(session.toString())).apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
        legacyPrefs.edit().clear().apply()
    }

    private fun migrateLegacySession() {
        if (!prefs.getString(SESSION_KEY, null).isNullOrBlank()) {
            legacyPrefs.edit().clear().apply()
            return
        }
        val oldAccess = legacyPrefs.getString("ACCESS_TOKEN", null)
        val oldRefresh = legacyPrefs.getString("REFRESH_TOKEN", null)
        if (!oldAccess.isNullOrBlank() && !oldRefresh.isNullOrBlank()) {
            saveTokens(
                accessToken = oldAccess,
                refreshToken = oldRefresh,
                role = legacyPrefs.getString("USER_ROLE", "").orEmpty(),
                userName = legacyPrefs.getString("USER_NAME", "")
            )
        } else {
            legacyPrefs.edit().clear().apply()
        }
    }

    private fun readSession(): JSONObject? {
        val encrypted = prefs.getString(SESSION_KEY, null) ?: return null
        return try {
            JSONObject(decrypt(encrypted))
        } catch (_: Exception) {
            // Un token inválido no debe dejar una sesión anterior activa.
            prefs.edit().clear().apply()
            null
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val combined = ByteBuffer.allocate(cipher.iv.size + encrypted.size)
            .put(cipher.iv)
            .put(encrypted)
            .array()
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        require(combined.size > GCM_IV_LENGTH) { "Invalid encrypted session." }
        val buffer = ByteBuffer.wrap(combined)
        val iv = ByteArray(GCM_IV_LENGTH).also(buffer::get)
        val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), javax.crypto.spec.GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val SECURE_PREFS = "auth_prefs_secure"
        const val LEGACY_PREFS = "auth_prefs"
        const val SESSION_KEY = "ENCRYPTED_SESSION_V1"
        const val KEY_ALIAS = "tradconnect_analytic_session_v1"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
    }
}
