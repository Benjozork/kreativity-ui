package com.katzstudio.kreativity.ui.event

import com.badlogic.gdx.math.Vector2

/**
 * The [KrMouseEvent] class contains parameters that describe generic mouse events.
 */
data class KrMouseEvent (

    val type: Type,

    val button: Button?,

    val deltaMove: Vector2?,

    val screenPosition: Vector2?,

    val isAltDown: Boolean = false,

    val isCtrlDown: Boolean = false,

    val isShiftDown: Boolean = false

) : KrEvent() {

    enum class Type {
        MOVED, PRESSED, RELEASED, DOUBLE_CLICK
    }

    enum class Button {
        LEFT, RIGHT, MIDDLE, NONE
    }

}
