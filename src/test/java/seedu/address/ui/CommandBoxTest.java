package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {
    @BeforeAll
    public static void setUpBeforeClass() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX toolkit has already been initialized.
        }
    }

    @Test
    public void hints_typingAndHistory_changeGuidanceWithoutExecuting() throws Exception {
        runOnFxThreadAndWait(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Success");
            });
            TextField input = getInput(box);
            Label hint = (Label) box.getRoot().lookup("#commandHint");
            assertTrue(hint.isVisible());
            assertTrue(hint.isManaged());
            assertTrue(hint.isWrapText());
            assertEquals(CommandHints.DEFAULT_HINT, hint.getText());
            for (String draft : List.of("a", "add", "add sn/", "listbyclass 999", "clear", "unknown", "")) {
                input.setText(draft);
                assertEquals(CommandHints.forInput(draft), hint.getText());
                assertTrue(executed.isEmpty());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            }
            submit(input, "list");
            assertEquals(List.of("list"), executed);
            assertEquals(CommandHints.DEFAULT_HINT, hint.getText());
            input.setText("edit 1");
            press(input, KeyCode.UP);
            assertEquals(CommandHints.forInput("list"), hint.getText());
            press(input, KeyCode.DOWN);
            assertEquals(CommandHints.forInput("edit"), hint.getText());
            assertEquals(List.of("list"), executed);
        });
    }

    @Test
    public void hints_longSyntax_wrapsBetweenInputAndOutputAtNarrowWidths() throws Exception {
        runOnFxThreadAndWait(() -> {
            CommandBox box = new CommandBox(command -> {
                throw new AssertionError("Layout must not execute commands");
            });
            ResultDisplay output = new ResultDisplay();
            VBox container = new VBox(box.getRoot(), output.getRoot());
            container.getStyleClass().add("app-shell");
            Scene scene = new Scene(container, 720, 600);
            scene.getStylesheets().add(getClass().getResource("/view/Workspace.css").toExternalForm());
            getInput(box).setText("edit 1");
            Label hint = (Label) box.getRoot().lookup("#commandHint");
            for (double width : new double[] {720, 450, 230, 720}) {
                container.resize(width, 600);
                container.applyCss();
                container.layout();
                container.layout();
                var hintBounds = hint.localToScene(hint.getBoundsInLocal());
                assertTrue(hintBounds.getMaxX() <= width + 1);
                assertTrue(hint.getHeight() >= hint.prefHeight(hint.getWidth()) - 1);
                assertEquals(hint.getText(), ((Text) hint.lookup(".text")).getText());
                assertTrue(getInput(box).localToScene(getInput(box).getBoundsInLocal()).getMaxY()
                        <= hintBounds.getMinY());
                assertTrue(hintBounds.getMaxY()
                        <= output.getRoot().localToScene(output.getRoot().getBoundsInLocal()).getMinY());
            }
        });
    }

    @Test
    public void navigate_recallsCommandsAndEditsWithoutExecutingAndRestoresDraft() throws Exception {
        runOnFxThreadAndWait(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Success");
            });
            TextField input = getInput(box);
            submit(input, "list");
            submit(input, "help");
            assertEquals("", input.getText());
            input.setText("draft");
            press(input, KeyCode.UP);
            assertEquals("help", input.getText());
            assertEquals(input.getLength(), input.getCaretPosition());
            input.setText("edited help");
            press(input, KeyCode.UP);
            assertEquals("list", input.getText());
            press(input, KeyCode.DOWN);
            assertEquals("edited help", input.getText());
            press(input, KeyCode.DOWN);
            assertEquals("draft", input.getText());
            press(input, KeyCode.SHIFT);
            assertEquals("draft", input.getText());
            assertEquals(List.of("list", "help"), executed);
        });
    }

    @Test
    public void submit_blankInput_doesNotExecuteOrSave() throws Exception {
        runOnFxThreadAndWait(() -> {
            CommandBox box = new CommandBox(command -> {
                throw new AssertionError("Blank input must not execute");
            });
            TextField input = getInput(box);
            submit(input, "   ");
            press(input, KeyCode.UP);
            assertEquals("   ", input.getText());
        });
    }

    @Test
    public void submit_failures_resetHistoryAndPreserveDraftAndErrorStyle() throws Exception {
        runOnFxThreadAndWait(() -> {
            CommandBox box = new CommandBox(command -> {
                if (command.equals("invalid")) {
                    throw new ParseException("Invalid syntax");
                }
                if (command.equals("delete 999")) {
                    throw new CommandException("Invalid index");
                }
                return new CommandResult("Success");
            });
            TextField input = getInput(box);
            submit(input, "list");
            press(input, KeyCode.UP);
            submit(input, "invalid");
            assertEquals("invalid", input.getText());
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            input.fireEvent(new ActionEvent());
            assertEquals(1, input.getStyleClass().stream()
                    .filter(CommandBox.ERROR_STYLE_CLASS::equals).count());
            press(input, KeyCode.UP);
            assertEquals("list", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            press(input, KeyCode.DOWN);
            assertEquals("invalid", input.getText());
            submit(input, "delete 999");
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            press(input, KeyCode.UP);
            assertEquals("list", input.getText());
            press(input, KeyCode.DOWN);
            assertEquals("delete 999", input.getText());
            submit(input, "help");
            press(input, KeyCode.UP);
            assertEquals("help", input.getText());
            press(input, KeyCode.UP);
            assertEquals("list", input.getText());
        });
    }

    @Test
    public void navigate_modifiedArrows_leaveInputAndHistoryPositionUnchanged() throws Exception {
        runOnFxThreadAndWait(() -> {
            CommandBox box = new CommandBox(command -> new CommandResult("Success"));
            TextField input = getInput(box);
            submit(input, "list");
            submit(input, "help");
            input.setText("draft");
            List<KeyCode> passedThrough = new ArrayList<>();
            EventHandler<KeyEvent> observer = event -> {
                assertFalse(event.isConsumed());
                passedThrough.add(event.getCode());
            };
            input.addEventFilter(KeyEvent.KEY_PRESSED, observer);
            for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN)) {
                for (int modifier = 0; modifier < 4; modifier++) {
                    input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                            modifier == 0, modifier == 1, modifier == 2, modifier == 3));
                    assertEquals("draft", input.getText());
                }
            }
            assertEquals(8, passedThrough.size());
            input.removeEventFilter(KeyEvent.KEY_PRESSED, observer);
            press(input, KeyCode.UP);
            assertEquals("help", input.getText());
            press(input, KeyCode.DOWN);
            assertEquals("draft", input.getText());
        });
    }

    @Test
    public void navigate_unchangedText_preservesUndoAndCaret() throws Exception {
        runOnFxThreadAndWait(() -> {
            CommandBox box = new CommandBox(command -> new CommandResult("Success"));
            TextField input = getInput(box);
            assertNavigationPreservesUndo(input, KeyCode.UP);
            assertNavigationPreservesUndo(input, KeyCode.DOWN);
            submit(input, "list");
            // Down at the draft cannot advance, even with saved history.
            assertNavigationPreservesUndo(input, KeyCode.DOWN);
            press(input, KeyCode.UP);
            // Up at the oldest entry cannot advance.
            assertNavigationPreservesUndo(input, KeyCode.UP);
        });
    }

    private static void assertNavigationPreservesUndo(TextField input, KeyCode code) {
        input.setText("draft text");
        input.deleteText(2, 5);
        String editedText = input.getText();
        input.positionCaret(1);
        assertTrue(input.isUndoable());
        press(input, code);
        assertEquals(editedText, input.getText());
        assertEquals(1, input.getCaretPosition());
        assertTrue(input.isUndoable());
        input.undo();
        assertEquals("draft text", input.getText());
    }

    private static TextField getInput(CommandBox box) {
        return (TextField) box.getRoot().lookup("#commandTextField");
    }

    private static void submit(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static void press(TextField input, KeyCode code) {
        input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
