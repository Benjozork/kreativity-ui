package com.katzstudio.kreativity.ui.dsl

import com.katzstudio.kreativity.ui.component.KrButton
import com.katzstudio.kreativity.ui.component.KrLabel
import com.katzstudio.kreativity.ui.component.KrWidget

fun KrWidget.label(text: String, init: KrLabel.() -> Unit = {}): KrLabel {
    return dslAddWidget(this, KrLabel(text), init)
}

fun KrWidget.button(text: String, init: KrButton.() -> Unit = {}): KrButton {
    return dslAddWidget(this, KrButton(text), init)
}

private fun <P : KrWidget, C : KrWidget> dslAddWidget(parent: P, child: C, init: C.() -> Unit): C {
    parent.add(child)
    child.init()
    child.invalidate()
    return child
}
