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
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 4) {
                        val ip = parts[0]
                        val mac = parts[3]
                        if (ip != "IP" && mac != "00:00:00:00:00:00" && mac.length == 17) {
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
}