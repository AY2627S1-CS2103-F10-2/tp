package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.AddressBook;

/**
 * Stores the latest 20 successful commands and preserves temporary edits while browsing them.
 */
public class CommandHistory {
    private static final Logger logger = LogsCenter.getLogger(CommandHistory.class);
    private static final int MAX_COMMANDS = AddressBook.MAX_COMMAND_HISTORY;

    private final List<String> commands = new ArrayList<>();
    private final List<String> workingCommands = new ArrayList<>();
    private int position;
    private String draft = "";

    /** Creates empty navigation history. */
    public CommandHistory() {
    }

    /** Restores successful commands without restoring drafts or navigation edits. */
    public CommandHistory(List<String> savedCommands) {
        requireNonNull(savedCommands).forEach(this::add);
    }

    /**
     * Records a successful, non-blank command and resets navigation to the end of history.
     */
    public void add(String command) {
        requireNonNull(command);
        if (command.isBlank()) {
            return;
        }
        commands.add(command);
        if (commands.size() > MAX_COMMANDS) {
            commands.remove(0);
            logger.fine("Oldest command removed: history limit reached");
        }
        logger.fine(() -> "Command added to history; size=" + commands.size());
        resetNavigation("");
    }

    /**
     * Returns to the end of history with the given draft, without recording a command.
     * Discards temporary edits by restoring the working copy from successful commands.
     * Used after a failed submission, including one made while browsing history.
     */
    public void resetNavigation(String currentInput) {
        draft = requireNonNull(currentInput);
        workingCommands.clear();
        workingCommands.addAll(commands);
        position = commands.size();
        logger.fine(() -> "History navigation reset to draft; temporary edits discarded; size=" + commands.size());
    }

    /**
     * Returns the previous command, preserving edits to the current input.
     * Stops at the oldest command.
     */
    public String previous(String currentInput) {
        requireNonNull(currentInput);
        if (commands.isEmpty()) {
            logger.fine("Previous-command navigation ignored: history is empty");
            return currentInput;
        }
        saveCurrentInput(currentInput);
        int previousPosition = position;
        position = Math.max(0, position - 1);
        if (previousPosition == position) {
            logger.fine("Previous-command navigation stopped: already at oldest entry");
        } else {
            logger.fine(() -> "Previous-command navigation; position=" + previousPosition + " -> " + position
                    + "; size=" + commands.size());
        }
        return workingCommands.get(position);
    }

    /**
     * Returns the next command, or restores the draft after the newest command.
     */
    public String next(String currentInput) {
        requireNonNull(currentInput);
        saveCurrentInput(currentInput);
        if (position == commands.size()) {
            logger.fine("Next-command navigation ignored: already at draft");
            return currentInput;
        }
        int previousPosition = position;
        position++;
        logger.fine(() -> "Next-command navigation; position=" + previousPosition + " -> " + position
                + "; size=" + commands.size() + "; at draft=" + (position == commands.size()));
        return position == commands.size() ? draft : workingCommands.get(position);
    }

    /**
     * Saves the input in the working copy or the separate draft, leaving saved commands unchanged.
     */
    private void saveCurrentInput(String currentInput) {
        requireNonNull(currentInput);
        if (position == commands.size()) {
            draft = currentInput;
        } else {
            workingCommands.set(position, currentInput);
        }
    }
}
