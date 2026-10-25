package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/** Checks shortcut handling and the lifecycle of the read-only list's scrolling skin. */
public class ReadOnlyListViewTest {

    @BeforeAll
    public static void initializeToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started the toolkit.
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void modifiedArrowKeys_doNotScrollOrSelectRows() throws Exception {
        runOnFxThreadAndWait(() -> {
            ReadOnlyListView<String> list = createList();
            StackPane container = attachToScene(list);
            try {
                VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                int firstIndex = flow.getFirstVisibleCell().getIndex();
                double position = flow.getPosition();
                for (int modifier = 0; modifier < 3; modifier++) {
                    list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN,
                            false, modifier == 1, modifier == 0, modifier == 2));
                    container.layout();
                    assertEquals(firstIndex, flow.getFirstVisibleCell().getIndex());
                    assertEquals(position, flow.getPosition());
                    assertNull(list.getSelectionModel());
                }
                // The same key without a modifier must still scroll.
                press(list, KeyCode.DOWN);
                container.layout();
                assertTrue(flow.getFirstVisibleCell().getIndex() > firstIndex);
            } finally {
                list.setSkin(null);
            }
        });
    }

    @Test
    public void emptyList_navigationKeys_doNotFallBackToSelection() throws Exception {
        runOnFxThreadAndWait(() -> {
            ReadOnlyListView<String> list = createList();
            list.getItems().clear();
            StackPane container = attachToScene(list);
            AtomicBoolean consumedBeforeDefaultHandler = new AtomicBoolean();
            list.addEventFilter(KeyEvent.KEY_PRESSED,
                    event -> consumedBeforeDefaultHandler.set(event.isConsumed()));
            AtomicInteger helpKeys = new AtomicInteger();
            container.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.F1) {
                    helpKeys.incrementAndGet();
                }
            });
            try {
                for (KeyCode code : List.of(KeyCode.END, KeyCode.HOME, KeyCode.PAGE_DOWN,
                        KeyCode.PAGE_UP, KeyCode.DOWN, KeyCode.UP, KeyCode.KP_DOWN, KeyCode.KP_UP)) {
                    assertDoesNotThrow(() -> press(list, code), code.toString());
                    assertTrue(consumedBeforeDefaultHandler.get(), code + " must not reach selection handlers");
                    container.layout();
                    assertTrue(list.getItems().isEmpty());
                    assertNull(list.getSelectionModel());
                }
                press(list, KeyCode.END);
                press(list, KeyCode.F1);
                assertFalse(consumedBeforeDefaultHandler.get());
                assertEquals(1, helpKeys.get(), "Help must remain available when the list is empty");
                assertDoesNotThrow(() -> list.scrollTo(0));
            } finally {
                list.setSkin(null);
            }
        });
    }

    @Test
    public void lastRow_variableHeightsAndSmallViewports_endAndScrollToRevealBottom() throws Exception {
        runOnFxThreadAndWait(() -> {
            for (double height : new double[] {40, 60, 119, 193.5}) {
                ReadOnlyListView<Integer> list = new ReadOnlyListView<>();
                list.getItems().setAll(IntStream.range(0, 32).boxed().toList());
                list.setCellFactory(view -> new ListCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : "Row " + item);
                        setPrefHeight(empty ? 24 : 37.25 + item % 5 * 17.5);
                    }
                });
                StackPane container = new StackPane(list);
                new Scene(container, 320, height);
                container.applyCss();
                container.layout();
                try {
                    VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                    for (boolean useKeyboard : new boolean[] {true, false}) {
                        press(list, KeyCode.HOME);
                        container.layout();
                        if (useKeyboard) {
                            press(list, KeyCode.END);
                        } else {
                            list.scrollTo(list.getItems().size() - 1);
                        }
                        container.layout();
                        container.layout();
                        assertEquals(31, flow.getLastVisibleCell().getIndex());
                        assertTrue(flow.getLastVisibleCell().getBoundsInParent().getMaxY()
                                <= flow.getHeight() + 1,
                                "The final row's bottom must be reachable at height " + height);
                        assertNull(list.getSelectionModel());
                    }
                } finally {
                    list.setSkin(null);
                }
            }
        });
    }

    @Test
    public void replaceSkin_detachesOldKeyboardHandlerAndRestoresScrolling() throws Exception {
        runOnFxThreadAndWait(() -> {
            ReadOnlyListView<String> list = createList();
            StackPane container = attachToScene(list);
            AtomicInteger unhandledKeys = new AtomicInteger();
            container.addEventHandler(KeyEvent.KEY_PRESSED, event -> unhandledKeys.incrementAndGet());
            try {
                Skin<?> originalSkin = list.getSkin();
                assertNotNull(originalSkin);
                press(list, KeyCode.DOWN);
                assertEquals(0, unhandledKeys.get());

                list.setSkin(null);
                assertNull(list.getSkin());
                press(list, KeyCode.DOWN);
                assertEquals(1, unhandledKeys.get(), "Disposed skin must not intercept keyboard events");

                // Reattach a skin as JavaFX does when a control is restyled or reused.
                list.setSkin(list.createDefaultSkin());
                container.applyCss();
                container.layout();
                assertNotSame(originalSkin, list.getSkin());
                VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                press(list, KeyCode.HOME);
                container.layout();
                int firstIndex = flow.getFirstVisibleCell().getIndex();
                press(list, KeyCode.DOWN);
                container.layout();
                assertTrue(flow.getFirstVisibleCell().getIndex() > firstIndex);
                assertEquals(1, unhandledKeys.get(), "Replacement skin must handle scrolling keys");
                assertNull(list.getSelectionModel());
            } finally {
                list.setSkin(null);
            }
        });
    }

    private static ReadOnlyListView<String> createList() {
        ReadOnlyListView<String> list = new ReadOnlyListView<>();
        list.setFixedCellSize(24);
        list.getItems().setAll(IntStream.range(0, 30).mapToObj(index -> "Class " + index).toList());
        return list;
    }

    private static StackPane attachToScene(ReadOnlyListView<?> list) {
        StackPane container = new StackPane(list);
        Scene scene = new Scene(container, 320, 192);
        assertNotNull(scene);
        container.applyCss();
        container.layout();
        return container;
    }

    private static void press(ReadOnlyListView<?> list, KeyCode code) {
        list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
