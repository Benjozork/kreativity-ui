package com.katzstudio.kreativity.ui.component;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.katzstudio.kreativity.ui.*;
import com.katzstudio.kreativity.ui.event.*;
import com.katzstudio.kreativity.ui.event.listener.KrFocusListener;
import com.katzstudio.kreativity.ui.event.listener.KrKeyboardListener;
import com.katzstudio.kreativity.ui.event.listener.KrMouseListener;
import com.katzstudio.kreativity.ui.model.KrAbstractItemModel;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.katzstudio.kreativity.ui.KrToolkit.setDefault;
import static com.katzstudio.kreativity.ui.TestObjectFactory.createButtonStyle;
import static com.katzstudio.kreativity.ui.TestObjectFactory.createLabelStyle;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests that kreativity components notify input listeners
 */
@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class InputListenerDispatchTest {

    @SuppressWarnings("unused")
    private final String testName;

    private final KrWidget testObject;

    static {
        KrFontMetrics fontMetricsMock = mock(KrFontMetrics.class);
        when(fontMetricsMock.bounds(any(), any())).thenReturn(new Rectangle(0, 0, 100, 10));
        KrToolkit toolkit = mock(KrToolkit.class);
        when(toolkit.fontMetrics()).thenReturn(fontMetricsMock);
        setDefault(toolkit);

        KrSkin.instance().setButtonStyle(createButtonStyle());
        KrSkin.instance().setLabelStyle(createLabelStyle());
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> testCases() {
        return Arrays.asList(new Object[][]{
                {"KrWidget", new KrWidget()},
                {"KrLabel", new KrLabel("")},
                {"KrButton", new KrButton("")},
                {"KrCheckbox", new KrCheckbox()},
                {"KrIconPanel", new KrIconPanel(KrFontAwesomeGlyph.FONT)},
                {"KrTextField", new KrTextField()},
                {"KrSpinner", new KrSpinner()},
                {"KrListView", new KrListView(mock(KrAbstractItemModel.class))},
                {"KrPanel", new KrPanel()},
                {"KrScrollBar", new KrScrollBar(KrOrientation.VERTICAL)},
                {"KrToggleButton", new KrToggleButton("")},
        });
    }

    @Test
    public void testMouseMoveListener() {
        KrMouseListener listener = mock(KrMouseListener.class);
        KrMouseEvent event = new KrMouseEvent(KrMouseEvent.Type.MOVED, null, null, null);

        testObject.addMouseListener(listener);
        testObject.handle(event);

        verify(listener).mouseMoved(event);
    }

    @Test
    public void testMouseEnterListener() {
        KrMouseListener listener = mock(KrMouseListener.class);
        KrEnterEvent event = new KrEnterEvent();

        testObject.addMouseListener(listener);
        testObject.handle(event);

        verify(listener).enter(event);
    }

    @Test
    public void testMouseExitListener() {
        KrMouseListener listener = mock(KrMouseListener.class);
        KrExitEvent event = new KrExitEvent();

        testObject.addMouseListener(listener);
        testObject.handle(event);

        verify(listener).exit(event);
    }

    @Test
    public void testMousePressedListener() {
        KrMouseListener listener = mock(KrMouseListener.class);
        KrMouseEvent event = new KrMouseEvent(KrMouseEvent.Type.PRESSED, KrMouseEvent.Button.LEFT, new Vector2(10, 10), new Vector2(100, 200));

        testObject.addMouseListener(listener);
        testObject.handle(event);

        verify(listener).mousePressed(event);
    }

    @Test
    public void testMouseReleasedListener() {
        KrMouseListener listener = mock(KrMouseListener.class);
        KrMouseEvent event = new KrMouseEvent(KrMouseEvent.Type.RELEASED, null, null, null);

        testObject.addMouseListener(listener);
        testObject.handle(event);

        verify(listener).mouseReleased(event);
    }

    @Test
    public void testMouseScrolledListener() {
        KrMouseListener listener = mock(KrMouseListener.class);
        KrScrollEvent event = new KrScrollEvent(10f);

        testObject.addMouseListener(listener);
        testObject.handle(event);

        verify(listener).scrolled(event);
    }

    @Test
    public void testKeyPressedListener() {
        KrKeyboardListener listener = mock(KrKeyboardListener.class);
        KrKeyEvent event = new KrKeyEvent(KrKeyEvent.Type.PRESSED, 0, "x");

        testObject.addKeyboardListener(listener);
        testObject.handle(event);

        verify(listener).keyPressed(event);
    }

    @Test
    public void testKeyReleasedListener() {
        KrKeyboardListener listener = mock(KrKeyboardListener.class);
        KrKeyEvent event = new KrKeyEvent(KrKeyEvent.Type.RELEASED, 0, "x");

        testObject.addKeyboardListener(listener);
        testObject.handle(event);

        verify(listener).keyReleased(event);
    }

    @Test
    public void testFocusGainedListener() {
        KrFocusListener listener = mock(KrFocusListener.class);
        KrFocusEvent event = new KrFocusEvent(KrFocusEvent.Type.FOCUS_GAINED);

        testObject.addFocusListener(listener);
        testObject.handle(event);

        verify(listener).focusGained(event);
    }

    @Test
    public void testFocusLostListener() {
        KrFocusListener listener = mock(KrFocusListener.class);
        KrFocusEvent event = new KrFocusEvent(KrFocusEvent.Type.FOCUS_LOST);

        testObject.addFocusListener(listener);
        testObject.handle(event);

        verify(listener).focusLost(event);
    }

    @Test
    public void testDoubleClickListener() {
        KrMouseListener listener = mock(KrMouseListener.class);
        KrMouseEvent event = new KrMouseEvent(KrMouseEvent.Type.DOUBLE_CLICK, KrMouseEvent.Button.LEFT, new Vector2(10, 10), new Vector2(200, 200));

        testObject.addMouseListener(listener);
        testObject.handle(event);

        verify(listener).mouseDoubleClicked(event);
    }
}
