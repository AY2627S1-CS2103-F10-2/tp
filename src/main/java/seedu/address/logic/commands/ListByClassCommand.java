package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_THRESHOLD;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;

/**
 * Lists students assigned to a specified academic class, optionally filtered by attendance count.
 */
public class ListByClassCommand extends Command {
    public static final String COMMAND_WORD = "listbyclass";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists students assigned to the class at the "
            + "specified index alphabetically, optionally filtered to those with fewer than THRESHOLD recorded "
            + "attendance dates.\n"
            + "Parameters: INDEX [" + PREFIX_THRESHOLD + "THRESHOLD]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_THRESHOLD + "5";
    public static final String MESSAGE_SUCCESS = "Listed %1$d students taking %2$s (%3$s).";
    public static final String MESSAGE_SUCCESS_WITH_THRESHOLD =
            "Listed %1$d students taking %2$s (%3$s) with fewer than %4$d recorded attendance dates.";

    private static final Logger logger = LogsCenter.getLogger(ListByClassCommand.class);

    private final Index index;
    private final Optional<Integer> threshold;

    /**
     * Creates a command to list the students in the class at the specified displayed index.
     */
    public ListByClassCommand(Index index) {
        this(index, Optional.empty());
    }

    /**
     * Creates a command to list the students in the class at the specified displayed index,
     * optionally restricted to students with fewer than {@code threshold} recorded attendance dates.
     */
    public ListByClassCommand(Index index, Optional<Integer> threshold) {
        requireAllNonNull(index, threshold);
        threshold.ifPresent(value -> checkArgument(value > 0, ParserUtil.MESSAGE_INVALID_POSITIVE_INTEGER));
        this.index = index;
        this.threshold = threshold;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        logger.fine("Listing students for displayed class index " + index.getOneBased()
                + threshold.map(value -> " with threshold " + value).orElse(""));
        List<AcademicClass> lastShownList = model.getFilteredAcademicClassList();
        if (index.getZeroBased() >= lastShownList.size()) {
            logger.warning("List-by-class rejected: invalid displayed class index " + index.getOneBased());
            throw new CommandException(Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        }
        AcademicClass academicClass = lastShownList.get(index.getZeroBased());
        model.filterPersonsByClass(academicClass, threshold);
        model.setAttendanceDisplayClass(academicClass);
        int count = model.getFilteredPersonList().size();
        logger.info("Listed " + count + " students for academic class "
                + academicClass.getModuleCode() + " (" + academicClass.getClassName() + ")"
                + threshold.map(value -> " with fewer than " + value + " recorded attendance dates").orElse(""));
        String feedback = threshold.map(value -> String.format(MESSAGE_SUCCESS_WITH_THRESHOLD, count,
                academicClass.getModuleCode(), academicClass.getClassName(), value))
                .orElseGet(() -> String.format(MESSAGE_SUCCESS, count,
                        academicClass.getModuleCode(), academicClass.getClassName()));
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ListByClassCommand otherCommand)) {
            return false;
        }
        return index.equals(otherCommand.index)
                && threshold.equals(otherCommand.threshold);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("threshold", threshold)
                .toString();
    }
}
