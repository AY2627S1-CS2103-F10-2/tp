package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;

/**
 * Deletes an academic class identified using its displayed index from the address book.
 */
public class DeleteClassCommand extends Command {

    public static final String COMMAND_WORD = "deleteclass";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the academic class identified by the index number used in the displayed class list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_CLASS_SUCCESS = "Deleted class: %1$s";

    private static final Logger logger = LogsCenter.getLogger(DeleteClassCommand.class);

    private final Index targetIndex;

    /**
     * Creates a command to delete the class at the given displayed index.
     */
    public DeleteClassCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<AcademicClass> lastShownList = model.getFilteredAcademicClassList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.fine("Class deletion rejected: displayed index is invalid.");
            throw new CommandException(Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        }

        AcademicClass classToDelete = lastShownList.get(targetIndex.getZeroBased());
        model.deleteAcademicClass(classToDelete);
        logger.info("Deleted academic class " + classToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_CLASS_SUCCESS, classToDelete));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteClassCommand otherDeleteClassCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteClassCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
