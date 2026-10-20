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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
