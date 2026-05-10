package com.shizuku.device.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shizuku.device.data.model.Device
import com.shizuku.device.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import javax.inject.Inject

data class DeviceListUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val shizukuAvailable: Boolean = false,
    val shizukuPermissionGranted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d("ShizukuDevice", "Binder received")
        checkShizuku()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d("ShizukuDevice", "Binder dead")
        _uiState.value = _uiState.value.copy(
            shizukuAvailable = false,
            shizukuPermissionGranted = false
        )
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        Log.d("ShizukuDevice", "Permission result: $grantResult")
        _uiState.value = _uiState.value.copy(
            shizukuPermissionGranted = grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
        if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            loadDevices()
        }
    }

    init {
        Log.d("ShizukuDevice", "ViewModel init")
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        checkShizuku()
    }

    fun checkShizuku() {
        val pingResult = try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            Log.e("ShizukuDevice", "pingBinder failed", e)
            false
        }
        Log.d("ShizukuDevice", "pingBinder result: $pingResult")

        val permissionResult = if (pingResult) {
            try {
                val result = Shizuku.checkSelfPermission()
                Log.d("ShizukuDevice", "checkSelfPermission result: $result")
                result == android.content.pm.PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                Log.e("ShizukuDevice", "checkSelfPermission failed", e)
                false
            }
        } else {
            false
        }

        Log.d("ShizukuDevice", "Available: $pingResult, Permission: $permissionResult")

        _uiState.value = _uiState.value.copy(
            shizukuAvailable = pingResult,
            shizukuPermissionGranted = permissionResult
        )

        if (pingResult && permissionResult) {
            loadDevices()
        }
    }

    fun refreshPermissionState() {
        checkShizuku()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val devices = deviceRepository.getConnectedDevices()
                _uiState.value = _uiState.value.copy(
                    devices = devices,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun requestShizukuPermission() {
        Log.d("ShizukuDevice", "Requesting permission")
        try {
            Shizuku.requestPermission(0)
        } catch (e: Exception) {
            Log.e("ShizukuDevice", "requestPermission failed", e)
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }
}