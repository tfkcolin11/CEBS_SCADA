package com.tfkcolin.cebs_scada.data

enum class AppMode{
    BLUETOOTH,
    GSM,
    WEB
}

data class DeviceData(
    val mode: AppMode,
    val address: String,
    val time: Long,
    val messages: ArrayList<String>
)
