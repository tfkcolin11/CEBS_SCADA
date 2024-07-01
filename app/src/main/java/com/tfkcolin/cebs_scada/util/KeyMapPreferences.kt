package com.tfkcolin.cebs_scada.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

class KeyMapPreferences(context: Context) {
    companion object{
        private const val PREFERENCE_NAME = "configuration"
        private const val FIRST_TIME = "isFirstRun"

        private const val ADMIN_PWD = "admin_pwd"

        private const val KEYBOARD_COLUMN = "keyboard_column"
        private const val LAST_DEVICE_ADDRESS = "last_device_address"
        private const val AUTO_CONNECT = "auto_connect"

        private const val ENCRYPTION_MODE = "encryption_mode"
        private const val APP_MODE = "app_mode"
        private const val DEVICE_PHONE_NUMBER = "phone_number"
        private const val UID = "uid"
        private const val USER_NAME = "user_name"
        private const val USER_PHONE = "user_phone"
        private const val USER_IMAGE = "user_img_url"
        private const val USER_EMAIL = "user_email"
        private const val USER_COUNTRY = "user_country"
        private const val USER_CITY = "user_city"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCE_NAME,
        Context.MODE_PRIVATE
    )

    fun encryptionMode() = preferences.getBoolean(ENCRYPTION_MODE, false)
    fun setEncryptionMode(mode: Boolean) =
        preferences.edit().putBoolean(APP_MODE, mode).commit()

    fun appMode() = preferences.getInt(APP_MODE, -1)
    fun setAppMode(mode: Int) =
        preferences.edit().putInt(APP_MODE, mode).commit()

    fun phoneNumber() = preferences.getString(DEVICE_PHONE_NUMBER, "")
    fun setPhoneNumber(number: String) =
        preferences.edit().putString(DEVICE_PHONE_NUMBER, number).commit()

    fun isFirstRun() = preferences.getBoolean(FIRST_TIME, true)
    fun setFirstRun() =
        preferences.edit().putBoolean(FIRST_TIME,false).commit()

    fun uid() = preferences.getString(UID, "")
    fun setUid(uid: String) =
        preferences.edit().putString(UID, uid).commit()

    fun pwd() = preferences.getString(ADMIN_PWD, "")
    fun setPwd(password: String) =
        preferences.edit().putString(ADMIN_PWD, password).commit()

    fun autoConnect() = preferences.getBoolean(AUTO_CONNECT, true)
    fun setAutoConnect(autoConnect: Boolean) =
        preferences.edit().putBoolean(AUTO_CONNECT, autoConnect).commit()

    fun lastDeviceAddress() = preferences.getString(LAST_DEVICE_ADDRESS, "")
    fun setLastDeviceAddress(address: String) =
        preferences.edit().putString(LAST_DEVICE_ADDRESS, address).commit()

    fun keyboardColumn() = preferences.getInt(KEYBOARD_COLUMN, 4)
    fun setKeyboardColumn(colNumber: Int) =
        preferences.edit().putInt(KEYBOARD_COLUMN, colNumber).commit()

    fun userName() = preferences.getString(USER_NAME, "")
    fun setUserName(name: String) =
        preferences.edit().putString(USER_NAME, name).commit()

    fun userPhone() = preferences.getString(USER_PHONE, "")
    fun setUserPhone(name: String) =
        preferences.edit().putString(USER_PHONE, name).commit()

    fun userImage() = preferences.getString(USER_IMAGE, "")
    fun setUserImage(name: String) =
        preferences.edit().putString(USER_IMAGE, name).commit()

    fun userEmail() = preferences.getString(USER_EMAIL, "")
    fun setUserEmail(name: String) =
        preferences.edit().putString(USER_EMAIL, name).commit()

    fun userCountry() = preferences.getString(USER_COUNTRY, "")
    fun setUserCountry(name: String) =
        preferences.edit().putString(USER_COUNTRY, name).commit()

    fun userCity() = preferences.getString(USER_CITY, "")
    fun setUserCity(name: String) =
        preferences.edit().putString(USER_CITY, name).commit()

    fun clearPreferences(){
        setUserImage("")
        setUserCountry("")
        setUserCity("")
        setUserPhone("")
        setUserEmail("")
        setUserName("")
        setUid("")
    }
}