package me.digitalnapotvrda.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import me.digitalnapotvrda.R


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

fun AppCompatActivity.setStatusBarColor(color: Int, lightStatusBar: Boolean) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = getColor(color)
    updateStatusBarTheme(lightStatusBar)
}

@Suppress("DEPRECATION")
private fun AppCompatActivity.updateStatusBarTheme(lightStatusBar: Boolean) {
    val flags = window.decorView.systemUiVisibility
    if (lightStatusBar) window.decorView.systemUiVisibility = flags or
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else window.decorView.systemUiVisibility =
        flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

}

fun Context.showYesOrNoDialog(
    messageResource: Int,
    positiveButtonListener: (() -> Unit),
    negativeButtonListener: (() -> Unit)? = null
) {
    AlertDialog.Builder(this).setMessage(messageResource).setCancelable(false).setPositiveButton(
        R.string.yes
    ) { _, _ ->
        positiveButtonListener()
    }.setNegativeButton(R.string.no) { dialog, _ ->
        if (negativeButtonListener == null) {
            dialog.dismiss()
        } else {
            negativeButtonListener()
        }
    }.show()
}

fun Context.dp2px(dp: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}