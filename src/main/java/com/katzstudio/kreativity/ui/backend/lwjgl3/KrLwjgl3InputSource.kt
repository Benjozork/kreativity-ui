package com.katzstudio.kreativity.ui.backend.lwjgl3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer

import com.katzstudio.kreativity.ui.backend.KrInputSource
import com.katzstudio.kreativity.ui.event.KrKeyEvent
import com.katzstudio.kreativity.ui.event.KrMouseEvent
import com.katzstudio.kreativity.ui.event.KrScrollEvent

import java.util.ArrayList
import java.util.Arrays

import com.badlogic.gdx.Input.Keys.*

/**
 * [KrInputSource] implementation for libgdx lwjgl3 backend.
 */
class KrLwjgl3InputSource : InputAdapter(), KrInputSource {

    private val listeners = ArrayList<KrInputSource.KrInputEventListener>()

    private var isAltDown: Boolean = false

    private var isCtrlDown: Boolean = false

    private var isShiftDown: Boolean = false

    private var isDragging: Boolean = false

    private var pressedKeyCode: Int = 0

    private val keyRepeat = true

    private var lastMousePressedTime = 0f

    private var lastMousePressedButton: KrMouseEvent.Button? = null

    private var keyRepeatTask = KeyRepeatTask()

    private var inputOffsetX = 0

    private var inputOffsetY = 0

    private val mousePosition = Vector2()

    init {
        // TODO: investigate pointer offset on MAC OSX. Compensating here with a small hack
        if ((System.getProperties()["os.name"] as String).contains("Mac")) {
            inputOffsetX = -4
            inputOffsetY = -3
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        isAltDown = isAltDown || isAlt(keycode)
        isCtrlDown = isCtrlDown || isCtrl(keycode)
        isShiftDown = isShiftDown || isShift(keycode)

        pressedKeyCode = keycode

        if (pressedKeyCode == LEFT || pressedKeyCode == RIGHT) {
            scheduleKeyRepeatTask(keycode)
        }

        val keyEvent = createKeyEvent(KrKeyEvent.Type.PRESSED, pressedKeyCode)

        notifyKeyPressed(keyEvent)

        return keyEvent.handled()
    }

    override fun keyTyped(character: Char): Boolean {
        if (hasStringRepresentation(pressedKeyCode)) {
            val keyEvent = createKeyEvent(KrKeyEvent.Type.PRESSED, character).copy(keycode = pressedKeyCode)
            notifyKeyPressed(keyEvent)
            return keyEvent.handled()
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        isAltDown = isAltDown && !isAlt(keycode)
        isCtrlDown = isCtrlDown && !isCtrl(keycode)
        isShiftDown = isShiftDown && !isShift(keycode)

        if (keyRepeat) {
            getKeyRepeatTask().cancel()
        }

        var keyEvent = createKeyEvent(KrKeyEvent.Type.RELEASED, keycode)
        if (isShiftDown) {
            keyEvent = keyEvent.copy(value = keyEvent.value!!.toUpperCase())
        }

        notifyKeyReleased(keyEvent)

        return keyEvent.handled()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, buttonIndex: Int): Boolean {
        var screenX = screenX
        var screenY = screenY

        screenX += inputOffsetX
        screenY += inputOffsetY

        isDragging = true
        val button = getButtonFor(buttonIndex)

        var mouseEvent = createMouseEvent(KrMouseEvent.Type.PRESSED, screenX, screenY, buttonIndex)

        val nanoTime = System.nanoTime()
        if (lastMousePressedTime == 0f) {
            lastMousePressedTime = nanoTime.toFloat()
            lastMousePressedButton = button
        } else {
            val deltaTime = nanoTime - lastMousePressedTime
            if (button == lastMousePressedButton && deltaTime < 200000000) {
                mouseEvent = createMouseEvent(KrMouseEvent.Type.DOUBLE_CLICK, screenX, screenY, buttonIndex)
            }
            lastMousePressedButton = button
            lastMousePressedTime = nanoTime.toFloat()
        }

        if (mouseEvent.type == KrMouseEvent.Type.PRESSED) {
            notifyMousePressed(mouseEvent)
        } else {
            notifyMouseDoubleClicked(mouseEvent)
        }

        return mouseEvent.handled()
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        var screenX = screenX
        var screenY = screenY

        screenX += inputOffsetX
        screenY += inputOffsetY

        isDragging = false
        val mouseEvent = createMouseEvent(KrMouseEvent.Type.RELEASED, screenX, screenY, button)

        notifyMouseReleased(mouseEvent)

        return mouseEvent.handled()
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        var screenX = screenX
        var screenY = screenY

        screenX += inputOffsetX
        screenY += inputOffsetY

        val mouseEvent = createMouseEvent(KrMouseEvent.Type.MOVED, screenX, screenY, -1)
        mousePosition.set(screenX.toFloat(), screenY.toFloat())

        notifyMouseMoved(mouseEvent)

        return mouseEvent.handled()
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        var screenX = screenX
        var screenY = screenY

        screenX += inputOffsetX
        screenY += inputOffsetY

        val mouseEvent = createMouseEvent(KrMouseEvent.Type.MOVED, screenX, screenY, -1)
        mousePosition.set(screenX.toFloat(), screenY.toFloat())

        notifyMouseMoved(mouseEvent)

        return mouseEvent.handled()
    }

    override fun scrolled(amount: Int): Boolean {
        val scrollEvent = KrScrollEvent(amount.toFloat())

        notifyScrolledEvent(scrollEvent)

        return scrollEvent.handled()
    }

    private fun createKeyEvent(type: KrKeyEvent.Type, keycode: Int): KrKeyEvent {
        return KrKeyEvent (
                type        = type,
                keycode     = keycode,
                value       = "",
                isAltDown   = isAltDown,
                isCtrlDown  = isCtrlDown,
                isShiftDown = isShiftDown
        )
    }

    private fun createKeyEvent(type: KrKeyEvent.Type, character: Char): KrKeyEvent {
        return KrKeyEvent (
                type        = type,
                keycode     = character.toInt(),
                value       = character.toString(),
                isAltDown   = isAltDown,
                isCtrlDown  = isCtrlDown,
                isShiftDown = isShiftDown
        )
    }

    private fun createMouseEvent(type: KrMouseEvent.Type, screenX: Int, screenY: Int, button: Int): KrMouseEvent {
        val eventButton = getButtonFor(button)
        val mousePosition = Vector2(screenX.toFloat(), screenY.toFloat())
        val mouseDelta = Vector2(Gdx.input.deltaX.toFloat(), Gdx.input.deltaY.toFloat())

        return KrMouseEvent (
                type           = type,
                button         = eventButton,
                screenPosition = mousePosition,
                deltaMove      = mouseDelta,
                isAltDown      = isAltDown,
                isCtrlDown     = isCtrlDown,
                isShiftDown    = isShiftDown
        )
    }

    fun scheduleKeyRepeatTask(keycode: Int) {
        if (!keyRepeat) {
            return
        }

        val keyRepeatTask = getKeyRepeatTask()
        if (!keyRepeatTask.isScheduled || keyRepeatTask.keycode != keycode) {
            keyRepeatTask.keycode = keycode
            keyRepeatTask.cancel()
            Timer.schedule(keyRepeatTask, KEY_REPEAT_INITIAL_TIME, KEY_REPEAT_TIME)
        }
    }

    private fun getKeyRepeatTask(): KeyRepeatTask {
        return keyRepeatTask
    }

    override fun isAltDown(): Boolean {
        return isAltDown
    }

    override fun isCtrlDown(): Boolean {
        return isCtrlDown
    }

    override fun isShiftDown(): Boolean {
        return isShiftDown
    }

    override fun isDragging(): Boolean {
        return isDragging
    }

    override fun getMousePosition(): Vector2 {
        return mousePosition
    }

    override fun addEventListener(listener: KrInputSource.KrInputEventListener) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: KrInputSource.KrInputEventListener) {
        listeners.remove(listener)
    }

    private fun notifyMouseMoved(event: KrMouseEvent) {
        listeners.forEach { l -> l.mouseMoved(event) }
    }

    private fun notifyMousePressed(event: KrMouseEvent) {
        listeners.forEach { l -> l.mousePressed(event) }
    }

    private fun notifyMouseReleased(event: KrMouseEvent) {
        listeners.forEach { l -> l.mouseReleased(event) }
    }

    private fun notifyMouseDoubleClicked(event: KrMouseEvent) {
        listeners.forEach { l -> l.mouseDoubleClicked(event) }
    }

    private fun notifyKeyPressed(event: KrKeyEvent) {
        listeners.forEach { l -> l.keyPressed(event) }
    }

    private fun notifyKeyReleased(event: KrKeyEvent) {
        listeners.forEach { l -> l.keyReleased(event) }
    }

    private fun notifyScrolledEvent(event: KrScrollEvent) {
        listeners.forEach { l -> l.scrolledEvent(event) }
    }

    /**
     * Used to schedule repeated key presses for arrows
     */
    private inner class KeyRepeatTask : Timer.Task() {
        var keycode: Int = 0

        override fun run() {
            keyDown(keycode)
        }
    }

    companion object {

        private const val KEY_REPEAT_INITIAL_TIME = 0.4f

        private const val KEY_REPEAT_TIME = 0.1f

        private val metaKeys = Arrays.asList(
                ALT_LEFT, ALT_RIGHT, CONTROL_LEFT, CONTROL_RIGHT, SHIFT_LEFT, SHIFT_RIGHT)

        private val textModifierKeys = Arrays.asList(
                DEL, BACKSPACE, FORWARD_DEL, ENTER, TAB)

        private val navigationKeys = Arrays.asList(
                LEFT, RIGHT, UP, DOWN, PAGE_DOWN, PAGE_UP, HOME, END, ESCAPE)

        private val functionKeys = Arrays.asList(
                F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12)

        private fun getButtonFor(button: Int): KrMouseEvent.Button {
            when (button) {
                Input.Buttons.LEFT -> return KrMouseEvent.Button.LEFT
                Input.Buttons.RIGHT -> return KrMouseEvent.Button.RIGHT
                Input.Buttons.MIDDLE -> return KrMouseEvent.Button.MIDDLE
            }

            return KrMouseEvent.Button.NONE
        }


        private fun hasStringRepresentation(keycode: Int): Boolean {
            return !metaKeys.contains(keycode) &&
                    !textModifierKeys.contains(keycode) &&
                    !navigationKeys.contains(keycode) &&
                    !functionKeys.contains(keycode)
        }

        private fun isAlt(keycode: Int): Boolean {
            return keycode == ALT_LEFT || keycode == ALT_RIGHT
        }

        private fun isCtrl(keycode: Int): Boolean {
            return keycode == CONTROL_LEFT || keycode == CONTROL_RIGHT
        }

        private fun isShift(keycode: Int): Boolean {
            return keycode == SHIFT_LEFT || keycode == SHIFT_RIGHT
        }
    }
}
