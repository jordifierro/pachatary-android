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

    @Throws(NoPersonInfoException::class)
    fun getPerson(): Person {
        val isRegistered = sharedPrefs.getBoolean(context.resources.getString(R.string.auth_person_is_registered_key),
                                                  false)
        val username = sharedPrefs.getString(context.resources.getString(R.string.auth_person_username_key), null)
        val email = sharedPrefs.getString(context.resources.getString(R.string.auth_person_email_key), null)
        val isEmailConfirmed = sharedPrefs.getBoolean(
                context.resources.getString(R.string.auth_person_is_email_confirmed_key), false)

        if (username == null) throw NoPersonInfoException("Person has not started register process")
        return Person(isRegistered = isRegistered, username = username,
                      email = email, isEmailConfirmed = isEmailConfirmed)
    }

    fun setPerson(person: Person) {
        val editor = sharedPrefs.edit()
        editor.putBoolean(context.resources.getString(R.string.auth_person_is_registered_key), person.isRegistered)
        editor.putString(context.resources.getString(R.string.auth_person_username_key), person.username)
        editor.putString(context.resources.getString(R.string.auth_person_email_key), person.email)
        editor.putBoolean(context.resources.getString(R.string.auth_person_is_email_confirmed_key),
                          person.isEmailConfirmed)
        editor.apply()
    }
}
