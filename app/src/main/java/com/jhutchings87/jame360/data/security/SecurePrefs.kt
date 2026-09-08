package com.jhutchings87.jame360.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Server profiles include plaintext-equivalent secrets (NZBGet password, Sonarr/Radarr
 * API keys), so they're stored in an AES-256-GCM encrypted preferences file backed by
 * the Android Keystore rather than plain SharedPreferences or an unencrypted DB.
 */
class SecurePrefs(context: Context) {

    val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "jame360_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
