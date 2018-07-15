package com.pachatary.data.auth

import android.content.Context
import com.pachatary.R


class AuthStorageRepository(val context: Context) {

    private val sharedPrefs = context.getSharedPreferences(
            context.resources.getString(R.string.auth_sharedpreferences_file), Context.MODE_PRIVATE)

    @Throws(NoLoggedException::class)
    fun getPersonCredentials(): AuthToken {
        val accessToken = sharedPrefs.getString(context.resources.getString(R.string.auth_access_token_key), null)
        val refreshToken = sharedPrefs.getString(context.resources.getString(R.string.auth_refresh_token_key), null)
        if (accessToken == null) throw NoLoggedException("Person has not logged in")
        return AuthToken(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun setPersonCredentials(authToken: AuthToken) {
        val editor = sharedPrefs.edit()
        editor.putString(context.resources.getString(R.string.auth_access_token_key), authToken.accessToken)
        editor.putString(context.resources.getString(R.string.auth_refresh_token_key), authToken.refreshToken)
        editor.apply()
    }

    fun isRegistrationCompleted(): Boolean =
        sharedPrefs.getBoolean(context.resources.getString(R.string.auth_is_register_completed_key),
                               false)

    fun setIsRegisterCompleted(isRegisterCompleted: Boolean) {
        val editor = sharedPrefs.edit()
        editor.putBoolean(context.resources.getString(R.string.auth_is_register_completed_key),
                          isRegisterCompleted)
        editor.apply()
    }
}
