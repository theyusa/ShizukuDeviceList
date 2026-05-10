package com.shizuku.device.data.repository

import com.shizuku.device.data.model.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File

class DeviceRepository {

    suspend fun getConnectedDevices(): List<Device> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<Device>()
        val ipNeighDevices = fetchFromIpNeigh()
        val arpDevices = fetchFromArp()
        val allDevices = (ipNeighDevices + arpDevices).distinctBy { it.mac }
        allDevices.forEach { device ->
            devices.add(device)
        }
        devices
    }

    private fun fetchFromIpNeigh(): List<Device> {
        val devices = mutableListOf<Device>()
        try {
            val result = Shizuku.newProcess(arrayOf("ip", "neigh", "show"), null, null)
            val output = result.inputStream.bufferedReader().readText()
            val error = result.errorStream.bufferedReader().readText()
            result.waitFor()
            if (result.exitValue() == 0 && output.isNotEmpty()) {
                parseIpNeighOutput(output, devices)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return devices
    }

    private fun parseIpNeighOutput(output: String, devices: MutableList<Device>) {
        output.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 5) {
                    val ip = parts[0]
                    val mac = parts[4]
                    if (isValidIp(ip) && isValidMac(mac)) {
                        devices.add(Device(ip = ip, mac = mac, isReachable = true))
                    }
                }
            }
        }
    }

    private fun fetchFromArp(): List<Device> {
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
        return devices
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