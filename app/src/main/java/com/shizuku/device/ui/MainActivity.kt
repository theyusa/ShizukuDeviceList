package com.shizuku.device.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.shizuku.device.ui.screens.DeviceListScreen
import com.shizuku.device.ui.screens.DeviceListViewModel
import com.shizuku.device.ui.theme.ShizukuDeviceTheme
import dagger.hilt.android.AndroidEntryPoint
import rikka.shizuku.Shizuku

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShizukuDeviceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeviceListScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Shizuku.pingBinder()
        Shizuku.checkSelfPermission()
    }
}