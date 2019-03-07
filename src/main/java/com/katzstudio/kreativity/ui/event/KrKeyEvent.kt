package com.katzstudio.kreativity.ui.event

/**
 * The [KrKeyEvent] class contains parameters that describe keyboard events.
 */
data class KrKeyEvent (

    val type: Type,

    val keycode: Int = 0,

    val value: String? = null,

    val isAltDown:   Boolean = false,

    val isCtrlDown:  Boolean = false,

    val isShiftDown: Boolean = false

) : KrEvent() {

    enum class Type {
        PRESSED, RELEASED
    }

}
