package me.digitalnapotvrda

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import me.digitalnapotvrda.utils.setStatusBarColor
import me.digitalnapotvrda.utils.showYesOrNoDialog
import me.digitalnapotvrda.utils.updateNavBarColor


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val viewModel by viewModels<MainViewModel>()

    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) showSettingsDialog()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DigitalnaPotvrda)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instantiateNavController()
        permissions.launch(Manifest.permission.CAMERA)
    }

    private fun instantiateNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        navGraph.startDestination = when {
            viewModel.isQrCodeAvailable() -> R.id.qrCodeFragment
            else -> R.id.scanFragment
        }

        navController.graph = navGraph

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.scanFragment -> {
                    updateNavBarColor(R.color.black, false)
                    setStatusBarColor(R.color.black, false)
                }
                R.id.qrCodeFragment -> {
                    updateNavBarColor(R.color.white, true)
                    setStatusBarColor(R.color.action, true)
                }
                else -> {
                    updateNavBarColor(R.color.white, true)
                    setStatusBarColor(R.color.white, true)
                }
            }
        }
    }

    private fun showSettingsDialog() {
        showYesOrNoDialog(R.string.go_to_settings_message, {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${this.packageName}")
            startActivity(intent)
        })
    }
}