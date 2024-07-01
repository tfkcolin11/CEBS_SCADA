package com.tfkcolin.cebs_scada.util

import com.tfkcolin.cebs_scada.data.*
import java.util.*
import java.util.regex.Pattern
import kotlin.text.StringBuilder

const val DIGITAL_READ_CMD = (101)
const val ANALOG_READ_CMD = (102)
const val DIGITAL_WRITE_CMD = (301)
const val ANALOG_WRITE_CMD = (302)
const val PWD_CHANGE_CMD = (401)

const val OK_RESPOND = (555)
const val INVALID_PWD = (901)
const val INVALID_CMD = (902)
const val INVALID_INPUT = (903)

const val DIGITAL_TYPE = (201)
const val ANALOG_TYPE = (202)

const val DIGITAL_P_D2 = (2)
const val DIGITAL_P_D4 = (4)
const val DIGITAL_P_D7 = (7)
const val DIGITAL_P_B0 = (8)
const val DIGITAL_P_B4 = (12)
const val DIGITAL_P_B5 = (13)

enum class AnalogPin{
    ANA_P_C0,
    ANA_P_C1,
    ANA_P_C2,
    ANA_P_C3,
    ANA_P_C4,
    ANA_P_C5
}

const val PWM_P_D3 = (3)
const val PWM_P_D5 = (5)
const val PWM_P_D6 = (6)
const val PWM_P_B1 = (9)
const val PWM_P_B2 = (10)
const val PWM_P_B3 = (11)

class Protocol private constructor() {
    var password: String = ""

    companion object{
        private var INSTANCE: Protocol? = null

        fun getInstance(): Protocol {
            var instance = INSTANCE
            if(instance == null){
                instance = Protocol()
                INSTANCE = instance
            }
            return INSTANCE!!
        }
    }

    fun composeCommand(type: Int, pin: Int, value: Int, pwd: String): String{
        return StringBuilder()
            .append(type)
            .append(pwd)
            .append(if (type == PWD_CHANGE_CMD) pwd else pin)
            .append(if (type == PWD_CHANGE_CMD) "" else (if(value > 1023) 1023 else value))
            .toString()
    }

    fun decomposeResponse(resp: String): PinValue {
        val pattern = Pattern.compile("""(\d+=\d+)""")
        return if(pattern.matcher(resp).matches()){
            val pin = resp.substringBefore('=').toIntOrNull() ?: 0
            val value = resp.substringAfter('=').toIntOrNull() ?: 0
            if(pin >= 14)
                AnalogPinValue(pin, value)
            else
                DigitalPinValue(pin, value > 0)
        }
        else{
            CommonPinValue(value = resp)
        }
    }
}
