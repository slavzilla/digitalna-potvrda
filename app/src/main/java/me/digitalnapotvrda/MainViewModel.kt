package me.digitalnapotvrda

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val prefs: SharedPreferences) : ViewModel() {

    fun isQrCodeAvailable(): Boolean {
        return !prefs.qrCode.isNullOrEmpty()
    }

    fun setQrCode(qrCode: String) {
        prefs.qrCode = qrCode
    }

    fun invalidateQrCode() {
        prefs.qrCode = null
        prefs.image = null
    }

    fun getQrCode(): String? {
        return prefs.qrCode
    }

    fun setBitmap(bitmap: Bitmap) {
        prefs.image = bitmap
    }

    fun getBitmap(): Bitmap? {
        return prefs.image
    }

}