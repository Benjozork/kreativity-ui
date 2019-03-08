package dsl

import com.katzstudio.kreativity.ui.component.KrPanel
import com.katzstudio.kreativity.ui.dsl.button
import com.katzstudio.kreativity.ui.dsl.label

import org.junit.Test

import org.hamcrest.MatcherAssert.assertThat

class KrWidgetsDslTest {

    @Test fun testDslAddWidgetsAndParams() {
        val panel = KrPanel()

        val b1 = panel.button("testButton")
        val l1 = panel.label("testLabel")

        assertThat("", panel.children.contains(b1) && b1.text == "testButton")
        assertThat("", panel.children.contains(l1) && l1.text == "testLabel")
    }

}