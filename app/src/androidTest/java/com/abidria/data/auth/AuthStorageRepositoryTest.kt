package com.abidria.data.auth

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.abidria.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthStorageRepositoryTest() {

    @Test
    fun test_get_and_save_auth_token() {
        given {
            an_auth_token()
        } whenn {
            save_that_auth_token()
        } then {
            get_should_receive_that_token()
        }
    }

    @Test
    fun test_get_when_no_auth_token() {
        given {
            nothing()
        } whenn {
            get_auth_token()
        } then {
            should_raise_no_logged_exception()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var authToken: AuthToken
        val authStorageRepository = AuthStorageRepository(InstrumentationRegistry.getTargetContext())
        lateinit var exception: Exception

        fun nothing() {}

        fun an_auth_token() {
            authToken = AuthToken("AT", "RT")
        }

        fun save_that_auth_token() {
            authStorageRepository.setPersonCredentials(authToken)
        }

        fun get_auth_token() {
            try {
                authStorageRepository.getPersonCredentials()
            } catch (e: NoLoggedException) {
                exception = e
            }
        }

        fun get_should_receive_that_token() {
            Assert.assertEquals(authStorageRepository.getPersonCredentials(), authToken)
        }

        fun should_raise_no_logged_exception() {
            assert(exception is NoLoggedException)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) {
            apply(func)
            shutDown()
        }

        private fun shutDown() {
            val context = InstrumentationRegistry.getTargetContext()
            val sharedPrefs = context.getSharedPreferences(
                context.resources.getString(R.string.auth_sharedpreferences_file), Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().commit()
        }
    }
}
