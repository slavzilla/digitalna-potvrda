package me.digitalnapotvrda

import android.R.attr
import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.lang.Exception


@Singleton
class SharedPreferences @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private const val PREF_FILENAME = "me.digitalnapotvrda"
        private const val QR_CODE = "QR_CODE"
        private const val IMAGE = "IMAGE"
    }

    private val prefs = context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE)

    var qrCode: String?
        get() = prefs.getString(QR_CODE, null)
        set(value) = prefs.edit().putString(QR_CODE, value).apply()

    var image: Bitmap?
    get() {
        val bytes = prefs.getString(IMAGE, null)
        return try {
            val encodeByte: ByteArray = Base64.decode(bytes, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }
        set(value) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            value?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val b: ByteArray = byteArrayOutputStream.toByteArray()
            val result = Base64.encodeToString(b, Base64.DEFAULT)
            prefs.edit().putString(IMAGE, result).apply()
        }

}