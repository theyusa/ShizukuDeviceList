package com.shizuku.device.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shizuku.device.data.model.Device
import com.shizuku.device.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceListUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val shizukuAvailable: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

    init {
        checkShizuku()
    }

    fun checkShizuku() {
        val available = try {
            val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
            val pingMethod = shizukuClass.getMethod("pingBinder")
            val result = pingMethod.invoke(null)
            result as Boolean
        } catch (e: Exception) {
            false
        }
        _uiState.value = _uiState.value.copy(shizukuAvailable = available)
        loadDevices()
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
        try {
            val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
            val requestMethod = shizukuClass.getMethod("requestPermission", Int::class.javaPrimitiveType)
            requestMethod.invoke(null, 0)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }
}