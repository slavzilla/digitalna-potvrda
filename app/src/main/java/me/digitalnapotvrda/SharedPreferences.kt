package me.digitalnapotvrda

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferences @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private const val PREF_FILENAME = "me.digitalnapotvrda"
        private const val QR_CODE = "QR_CODE"
    }

    private val prefs = context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE)

    var qrCode: String?
        get() = prefs.getString(QR_CODE, null)
        set(value) = prefs.edit().putString(QR_CODE, value).apply()

}