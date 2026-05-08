package com.shizuku.device.data.model

data class Device(val ip: String, val mac: String, val isReachable: Boolean = true)