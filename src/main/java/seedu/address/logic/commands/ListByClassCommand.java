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
 * Lists students assigned to a specified academic class.
 */
public class ListByClassCommand extends Command {
    public static final String COMMAND_WORD = "listbyclass";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists students assigned to the class at the "
            + "specified index alphabetically.\n"
            + "Parameters: INDEX\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Listed %1$d students taking %2$s (%3$s).";

    private static final Logger logger = LogsCenter.getLogger(ListByClassCommand.class);

    private final Index index;

    /**
     * Creates a command to list the students in the class at the specified displayed index.
     */
    public ListByClassCommand(Index index) {
        this.index = requireNonNull(index);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        logger.fine("Listing students for displayed class index " + index.getOneBased());
        List<AcademicClass> lastShownList = model.getFilteredAcademicClassList();
        if (index.getZeroBased() >= lastShownList.size()) {
            logger.warning("List-by-class rejected: invalid displayed class index " + index.getOneBased());
            throw new CommandException(Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        }
        AcademicClass academicClass = lastShownList.get(index.getZeroBased());
        model.filterPersonsByClass(academicClass);
        model.setAttendanceDisplayClass(academicClass);
        logger.info("Listed " + model.getFilteredPersonList().size() + " students for academic class "
                + academicClass.getModuleCode() + " (" + academicClass.getClassName() + ")");
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getFilteredPersonList().size(),
                academicClass.getModuleCode(), academicClass.getClassName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ListByClassCommand otherCommand)) {
            return false;
        }
        return index.equals(otherCommand.index);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .toString();
    }
}
