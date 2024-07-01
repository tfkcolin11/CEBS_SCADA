package com.tfkcolin.cebs_scada.util

enum class DeviceList {
    BLUETOOTH_DEVICE,
    SMS_DEVICE,
    WEB_SERVICE
}
interface DeviceDataObserver {
    fun update(list: DeviceList)
}

interface DeviceDataObservable {
    fun registerObserver(observer: DeviceDataObserver)
    fun unRegisterObserver(observer: DeviceDataObserver)
    fun notifyObservers()
}