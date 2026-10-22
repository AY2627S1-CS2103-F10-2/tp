package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {
    @Test
    public void navigate_emptyHistory_preservesInput() {
        CommandHistory history = new CommandHistory();
        assertEquals("draft", history.previous("draft"));
        assertEquals("draft", history.next("draft"));
    }

    @Test
    public void navigate_stopsAtBoundariesAndRestoresDraft() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("help");
        assertEquals("help", history.previous("unfinished command"));
        assertEquals("list", history.previous("help"));
        assertEquals("list", history.previous("list"));
        assertEquals("help", history.next("list"));
        assertEquals("unfinished command", history.next("help"));
        assertEquals("new draft", history.next("new draft"));
        assertEquals("help", history.previous("new draft"));
        assertEquals("new draft", history.next("help"));
    }

    @Test
    public void add_moreThanTwentyCommands_discardsOldest() {
        CommandHistory history = new CommandHistory();
        for (int i = 1; i <= 25; i++) {
            history.add("command " + i);
        }
        for (int i = 25; i >= 6; i--) {
            String currentInput = i == 25 ? "" : "command " + (i + 1);
            assertEquals("command " + i, history.previous(currentInput));
        }
        assertEquals("command 6", history.previous("command 6"));
    }

    @Test
    public void add_whileBrowsing_resetsNavigationAndPreservesOriginalEntry() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        assertEquals("list", history.previous("draft"));
        history.add("help");
        assertEquals("help", history.previous(""));
        assertEquals("list", history.previous("help"));
        assertEquals("help", history.next("list"));
        assertEquals("", history.next("help"));
    }

    @Test
    public void resetNavigation_failedRecall_restoresFailedInputWithoutSavingIt() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("help");
        history.previous("old draft");
        history.previous("help");
        history.resetNavigation("invalid command");
        assertEquals("help", history.previous("invalid command"));
        assertEquals("invalid command", history.next("help"));
        history.add("list");
        assertEquals("list", history.previous(""));
        assertEquals("help", history.previous("list"));
        assertEquals("list", history.previous("help"));
        assertEquals("list", history.previous("list"));
    }

    @Test
    public void add_blankIgnoredAndRepeatedCommandsRetained() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("list");
        history.add("");
        history.add("   ");
        assertEquals("list", history.previous("draft"));
        assertEquals("list", history.previous("list"));
        assertEquals("list", history.next("list"));
        assertEquals("draft", history.next("list"));
    }

    @Test
    public void navigate_multipleEditedEntries_preservesEditsAndDraft() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("help");
        assertEquals("help", history.previous("draft"));
        assertEquals("list", history.previous("edited help"));
        assertEquals("edited list", history.previous("edited list"));
        assertEquals("edited help", history.next("edited list"));
        assertEquals("draft", history.next("edited help"));
        assertEquals("edited help", history.previous("draft"));
        assertEquals("edited list", history.previous("edited help"));
    }

    @Test
    public void add_success_rebuildsWorkingCopyFromOriginalCommands() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("help");
        history.previous("draft");
        history.previous("edited help");
        history.next("edited list");
        history.add("edited help");
        assertEquals("edited help", history.previous(""));
        assertEquals("help", history.previous("edited help"));
        assertEquals("list", history.previous("help"));
    }

    @Test
    public void resetNavigation_failure_discardsWorkingEditsAndPreservesFailedDraft() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("help");
        history.previous("draft");
        history.previous("edited help");
        history.resetNavigation("invalid list");
        assertEquals("help", history.previous("invalid list"));
        assertEquals("list", history.previous("help"));
        assertEquals("help", history.next("list"));
        assertEquals("invalid list", history.next("help"));
    }
}
