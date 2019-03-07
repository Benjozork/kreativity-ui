package com.katzstudio.kreativity.ui.event

/**
 * The [KrScrollEvent] class contains parameters that describe mouse scroll events.
 */
data class KrScrollEvent(val scrollAmount: Float = 0.toFloat()) : KrEvent()
