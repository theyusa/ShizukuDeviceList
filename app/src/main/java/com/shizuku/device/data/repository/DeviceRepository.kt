package com.shizuku.device.data.repository

import com.shizuku.device.data.model.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DeviceRepository {

    suspend fun getConnectedDevices(): List<Device> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<Device>()
        try {
            val file = File("/proc/net/arp")
            if (file.exists()) {
                file.forEachLine { line ->
                    val parts = line.trim().split("\\s+".toRegex())
                    if (parts.size >= 6) {
                        val ip = parts[0]
                        val mac = parts[3]
                        if (isValidIp(ip) && isValidMac(mac)) {
                            devices.add(Device(ip = ip, mac = mac, isReachable = true))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        devices
    }

    private fun isValidIp(ip: String): Boolean {
        val ipRegex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
        return ipRegex.matches(ip)
    }

    private fun isValidMac(mac: String): Boolean {
        val macRegex = Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$")
        return macRegex.matches(mac)
    }
}