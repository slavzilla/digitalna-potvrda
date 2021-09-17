package me.digitalnapotvrda.utils

import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.updateNavBarColor(color: Int, isLight: Boolean) {
    setNavBarColor(color)
    setNavBarLightDark(isLight)
}

private fun AppCompatActivity.setNavBarColor(color: Int) {
    val colorResource = resources.getColor(color, null)
    if (Build.VERSION.SDK_INT >= 27) {
        window.navigationBarColor = colorResource
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = colorResource
        }
    }
}

@Suppress("DEPRECATION")
private fun AppCompatActivity.setNavBarLightDark(isLight: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    var flags = window.decorView.systemUiVisibility
    flags = if (isLight) {
        flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
    } else {
        flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.decorView.systemUiVisibility = flags

}