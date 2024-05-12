package com.tfkcolin.cebs_scada.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.*

val ARDUINO_UNO_PINS_ID = listOf(
    "0", "1", "2",
    "3", "4", "5",
    "6", "7", "8",
    "9", "10", "11",
    "12", "13", "A0",
    "A1", "A2", "A3",
    "A4", "A5"
)

@Entity(tableName = "pin_event")
data class PinEvent(
    @PrimaryKey
    val mcPinId: String,
    val eventsMap: Map<String, String> = mapOf()
)

@Entity(tableName = "map_key")
data class MapKey(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String = "",
    val cmd: String = "",
    val selected: Boolean = true
)

class MapConverter {
    @TypeConverter
    fun toJson(map: Map<String, String>): String{
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return moshi.adapter<Map<String, String>>(type).toJson(map)
    }
    @TypeConverter
    fun toMap(str: String): Map<String, String>{
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return moshi.adapter<Map<String, String>>(type).fromJson(str) ?: mapOf()
    }
}

/**
 *  interface to hold the received data from network ana map then to pin event
 */
interface PinValue{
    val pin: Int
    val time: Long
}

open class NumericPinValue(
    override val pin: Int,
    open val value: Int,
    override val time: Long = Calendar.getInstance().timeInMillis
): PinValue

data class DigitalPinValue(
    override val pin: Int,
    val value: Boolean,
    override val time: Long = Calendar.getInstance().timeInMillis
): PinValue

data class CommonPinValue(
    override val pin: Int = -1,
    val value: String,
    override val time: Long = Calendar.getInstance().timeInMillis
): PinValue

data class StatusPinValue(
    override val pin: Int,
    override val value: Int,
    override val time: Long = Calendar.getInstance().timeInMillis
): NumericPinValue(pin, value, time)

data class AnalogPinValue(
    override val pin: Int,
    override val value: Int,
    override val time: Long = Calendar.getInstance().timeInMillis
): NumericPinValue(pin, value, time)