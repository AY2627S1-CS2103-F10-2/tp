package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;

/**
 * Adds a new academic class to the address book.
 */
public class AddClassCommand extends Command {

    public static final String COMMAND_WORD = "addclass";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an academic class to the address book. "
            + "Parameters: "
            + PREFIX_MODULE_CODE + "MODULE_CODE "
            + PREFIX_CLASS_NAME + "CLASS_NAME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_MODULE_CODE + "CS2103 "
            + PREFIX_CLASS_NAME + "F10-2";

    public static final String MESSAGE_SUCCESS = "New class added: %1$s";
    public static final String MESSAGE_DUPLICATE_CLASS = "A class with this name for this module already exists";

    private final AcademicClass toAdd;

    /**
     * Creates an AddClassCommand for the specified {@code AcademicClass}.
     */
    public AddClassCommand(AcademicClass academicClass) {
        requireNonNull(academicClass);
        toAdd = academicClass;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasAcademicClass(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_CLASS);
        }

        model.addAcademicClass(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddClassCommand otherAddClassCommand)) {
            return false;
        }

        return toAdd.equals(otherAddClassCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
