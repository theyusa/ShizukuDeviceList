package com.shizuku.device.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.shizuku.device.ui.screens.DeviceListScreen
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
        try {
            if (Shizuku.pingBinder()) {
                Log.d("ShizukuDevice", "MainActivity onResume: binder available")
            }
        } catch (e: Exception) {
            Log.e("ShizukuDevice", "MainActivity onResume error", e)
        }
    }
}