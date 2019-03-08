package com.katzstudio.kreativity.ui.util

import com.katzstudio.kreativity.ui.KrCanvas
import com.katzstudio.kreativity.ui.KrToolkit

/**
 * The [KrTimer] class is a special timer that's updated by the [KrCanvas].
 */
class KrTimer : KrUpdateListener {

    private var isRegistered = false

    private var timePassed = 0.0f

    private var delay: Float = 0.toFloat()

    private var period: Float = 0.toFloat()

    private var passedDelay = false

    private var runnable: Runnable? = null

    constructor(delay: Float, period: Float = 0f, runnable: Runnable) {
        this.delay = delay
        this.period = period
        this.runnable = runnable
    }

    fun start() {
        if (!isRegistered) {
            KrToolkit.getDefaultToolkit().registerUpdateListener(this)
            isRegistered = true
        }
        timePassed = 0f
        passedDelay = false
    }

    fun stop() {
        KrToolkit.getDefaultToolkit().unregisterUpdateListener(this)
        isRegistered = false
    }

    fun restart() {
        stop()
        start()
    }

    override fun update(deltaSeconds: Float) {
        timePassed += deltaSeconds

        // first check delay
        if (!passedDelay && timePassed > delay) {
            passedDelay = true

            callRunnable()

            if (period == 0f) {
                stop()
            } else {
                timePassed -= delay
            }
        }

        if (passedDelay && timePassed > period) {
            callRunnable()
            timePassed -= period
        }
    }

    private fun callRunnable() {
        if (runnable != null) {
            runnable!!.run()
        }
    }

}