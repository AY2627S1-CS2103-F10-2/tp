package seedu.address.ui;

import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyEvent;

/** A focusable list with keyboard scrolling and no row selection. */
public class ReadOnlyListView<T> extends ListView<T> {

    /** Creates a list that can receive keyboard focus without selecting rows. */
    public ReadOnlyListView() {
        setSelectionModel(null);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ReadOnlyListViewSkin();
    }

    /** Reuses JavaFX's virtualized scrolling independently of row selection. */
    private class ReadOnlyListViewSkin extends ListViewSkin<T> {
        private final EventHandler<KeyEvent> scrollHandler = this::handleScrollKey;

        ReadOnlyListViewSkin() {
            super(ReadOnlyListView.this);
            addEventFilter(KeyEvent.KEY_PRESSED, scrollHandler);
        }

        private void handleScrollKey(KeyEvent event) {
            if (event.isAltDown() || event.isControlDown() || event.isMetaDown()) {
                return;
            }
            VirtualFlow<ListCell<T>> flow = getVirtualFlow();
            ListCell<T> first = flow.getFirstVisibleCell();
            if (first == null) {
                // JavaFX 17's default Home/End handlers assume a non-null selection model.
                switch (event.getCode()) {
                    case UP, KP_UP, DOWN, KP_DOWN, PAGE_UP, PAGE_DOWN, HOME, END -> event.consume();
                    default -> {
                        // Leave focus traversal and accelerators available when the list is empty.
                    }
                }
                return;
            }
            switch (event.getCode()) {
                case UP, KP_UP -> flow.scrollPixels(-first.getHeight());
                case DOWN, KP_DOWN -> flow.scrollPixels(first.getHeight());
                case PAGE_UP -> flow.scrollPixels(-flow.getHeight());
                case PAGE_DOWN -> flow.scrollPixels(flow.getHeight());
                case HOME -> flow.scrollToTop(0);
                case END -> flow.scrollTo(getItemCount() - 1);
                default -> {
                    return;
                }
            }
            event.consume();
        }

        @Override
        public void dispose() {
            removeEventFilter(KeyEvent.KEY_PRESSED, scrollHandler);
            super.dispose();
        }
    }
}
