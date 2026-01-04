package com.cdi.moonphase

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cdi.moonphase.data.repository.MoonRepository
import com.cdi.moonphase.domain.usecase.GetCurrentMoonPhaseUseCase
import com.cdi.moonphase.domain.usecase.ScheduleMoonNotificationUseCase
import com.cdi.moonphase.presentation.MoonPhaseScreen
import com.cdi.moonphase.presentation.MoonPhaseViewModel
import com.cdi.moonphase.ui.theme.MoonPhaseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = MoonRepository()
                val getPhase = GetCurrentMoonPhaseUseCase()
                val schedule = ScheduleMoonNotificationUseCase(applicationContext, repo)
                return MoonPhaseViewModel(getPhase, schedule) as T
            }
        })[MoonPhaseViewModel::class.java]

        setContent {
            MoonPhaseTheme {
                MoonPhaseScreen(viewModel)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }
}