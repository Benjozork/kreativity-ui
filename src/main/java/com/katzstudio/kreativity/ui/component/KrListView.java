package com.katzstudio.kreativity.ui.component;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.katzstudio.kreativity.ui.KrSelectionMode;
import com.katzstudio.kreativity.ui.component.renderer.KrListViewCellRenderer;
import com.katzstudio.kreativity.ui.event.KrMouseEvent;
import com.katzstudio.kreativity.ui.event.KrScrollEvent;
import com.katzstudio.kreativity.ui.layout.KrFlowLayout;
import com.katzstudio.kreativity.ui.layout.KrLayout;
import com.katzstudio.kreativity.ui.model.KrAbstractItemModel;
import com.katzstudio.kreativity.ui.model.KrAbstractItemModel.KrModelIndex;
import com.katzstudio.kreativity.ui.model.KrSelection;
import com.katzstudio.kreativity.ui.model.KrSelectionModel;
import com.katzstudio.kreativity.ui.style.KrWidgetStyle;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.katzstudio.kreativity.ui.KrOrientation.VERTICAL;
import static com.katzstudio.kreativity.ui.KrToolkit.getDefaultToolkit;

/**
 * A list widget displays items in a vertical list. Items can be selected.
 */
public class KrListView extends KrWidget<KrWidgetStyle> {

    private final KrAbstractItemModel model;

    private final KrPanel innerPanel;

    private final Renderer renderer;

    private int listGeometryOffset = 0;

    private KrScrollBar verticalScrollBar = new KrScrollBar(VERTICAL);

    private List<KrItemDelegate> delegates = new ArrayList<>();

    private List<KrDoubleClickListener> doubleClickListeners = new ArrayList<>();

    @Getter private KrSelectionModel selectionModel = new KrSelectionModel();

    @Getter private KrSelectionMode selectionMode = KrSelectionMode.EXTENDED;

    public KrListView(KrAbstractItemModel model) {
        this(model, new KrListViewCellRenderer());
    }

    public KrListView(KrAbstractItemModel model, Renderer renderer) {
        this.model = model;
        this.renderer = renderer;
        this.innerPanel = new KrPanel(new KrFlowLayout(VERTICAL));

        model.addListener(this::onModelDataChanged);
        verticalScrollBar.addScrollListener(this::onScroll);
        selectionModel.addSelectionListener(this::onSelectionChanged);

        setStyle(getDefaultToolkit().getSkin().getListViewStyle());

        setLayout(new Layout());
        add(innerPanel);
        add(verticalScrollBar);

        onModelDataChanged();
    }

    private void onScroll(float v) {
        listGeometryOffset = (int) v;
        invalidate();
    }

    private void onSelectionChanged(KrSelection oldSelection, KrSelection newSelection) {
        for (KrModelIndex index : oldSelection) {
            delegates.get(index.getRow()).setSelected(false);
        }

        for (KrModelIndex index : newSelection) {
            delegates.get(index.getRow()).setSelected(true);
        }
    }

    public void setSelectionMode(KrSelectionMode newSelectionMode) {
        if (newSelectionMode != selectionMode) {
            selectionMode = newSelectionMode;
            selectionModel.clearSelection();
        }
    }

    @Override
    protected boolean mousePressedEvent(KrMouseEvent event) {
        super.mousePressedEvent(event);

        if (selectionMode == KrSelectionMode.NONE) {
            return false;
        }

        KrModelIndex itemIndex = findItemIndexAt(screenToLocal(event.getScreenPosition()));
        if (selectionMode == KrSelectionMode.SINGLE) {
            if (event.isCtrlDown() && selectionModel.getCurrentSelection().contains(itemIndex)) {
                selectionModel.setSelection(KrSelection.EMPTY);
            } else {
                selectionModel.setSelection(KrSelection.of(itemIndex));
            }
            return true;
        }

        if (event.isCtrlDown()) {
            if (selectionModel.getCurrentSelection().contains(itemIndex)) {
                selectionModel.remove(itemIndex);
            } else {
                selectionModel.add(itemIndex);
            }
        } else {
            selectionModel.setSelection(KrSelection.of(itemIndex));
        }
        return true;
    }

    @Override
    protected boolean mouseDoubleClickEvent(KrMouseEvent event) {
        super.mouseDoubleClickEvent(event);

        KrModelIndex itemIndex = findItemIndexAt(screenToLocal(event.getScreenPosition()));
        if (itemIndex != null) {
            notifyItemDoubleClicked(itemIndex);
        }
        return true;
    }

    @Override
    protected boolean scrollEvent(KrScrollEvent event) {
        super.scrollEvent(event);
        return verticalScrollBar.scrollEvent(event);
    }

    @Override
    public void ensureUniqueStyle() {
        if (style == getDefaultToolkit().getSkin().getListViewStyle()) {
            style = new KrWidgetStyle(style);
        }
    }

    private void onModelDataChanged() {
        innerPanel.removeAll();
        delegates.clear();

        for (int i = 0; i < model.getRowCount(); ++i) {
            KrModelIndex index = new KrModelIndex(i);
            KrItemDelegate itemDelegate = renderer.getComponent(index, model);
            delegates.add(itemDelegate);
            innerPanel.add(itemDelegate.getWidget());
        }

        invalidate();
    }

    private KrModelIndex findItemIndexAt(Vector2 position) {
        return findItemIndexAt((int) position.x, (int) position.y);
    }

    private KrModelIndex findItemIndexAt(int x, int y) {
        int itemIndex = 0;
        for (KrItemDelegate delegate : delegates) {
            if (delegate.getWidget().getGeometry().contains(x, y + listGeometryOffset)) {
                return new KrModelIndex(itemIndex);
            }
            itemIndex += 1;
        }
        return null;
    }

    public void addDoubleClickListener(KrDoubleClickListener listener) {
        doubleClickListeners.add(listener);
    }

    public void removeDoubleClickListener(KrDoubleClickListener listener) {
        doubleClickListeners.remove(listener);
    }

    protected void notifyItemDoubleClicked(KrModelIndex itemIndex) {
        doubleClickListeners.forEach(l -> l.itemDoubleClicked(itemIndex));
    }

    public interface Renderer {
        KrItemDelegate getComponent(KrModelIndex index, KrAbstractItemModel model);
    }

    private class Layout implements KrLayout {

        @Override
        public void setGeometry(Rectangle geometry) {
            int width = (int) geometry.getWidth();
            int height = (int) geometry.getHeight();

            float preferredHeight = innerPanel.getPreferredHeight();
            innerPanel.setGeometry(0, -listGeometryOffset, width, preferredHeight);

            int requiredScrollSize = (int) (preferredHeight - geometry.getHeight());

            if (requiredScrollSize > 0) {
                int scrollBarWidth = (int) verticalScrollBar.getPreferredWidth();
                verticalScrollBar.setGeometry(width - scrollBarWidth, 0, scrollBarWidth, height);
                verticalScrollBar.setValueRange(0, requiredScrollSize);
            } else {
                verticalScrollBar.setSize(0, 0);
                verticalScrollBar.setValueRange(0, 0);
            }
        }

        @Override
        public Vector2 getMinSize() {
            return innerPanel.getMinSize();
        }

        @Override
        public Vector2 getMaxSize() {
            return innerPanel.getMaxSize();
        }

        @Override
        public Vector2 getPreferredSize() {
            return innerPanel.getPreferredSize();
        }

        @Override
        public void addWidget(KrWidget child, Object layoutConstraint) {
        }

        @Override
        public void removeWidget(KrWidget child) {
        }
    }

    public interface KrDoubleClickListener {
        void itemDoubleClicked(KrModelIndex itemIndex);
    }

    /**
     * The item delegate wraps the {@link KrWidget} displayed in the list view.
     * The delegate can alter the widget in various ways, depending on what the list view
     * requests. For instance, it can change the widget's appearance when selected.
     */
    public static abstract class KrItemDelegate {

        /**
         * Sets whether or not the underlying widget is selected or not
         *
         * @param selected the selection status of the widget
         */
        abstract public void setSelected(boolean selected);

        /**
         * Returns the widget that's used by the list view to display an item.
         *
         * @return the widget managed by this delegate.
         */
        abstract public KrWidget getWidget();
    }
}
