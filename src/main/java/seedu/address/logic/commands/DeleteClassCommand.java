package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Deletes an academic class identified by its module code and class name.
 */
public class DeleteClassCommand extends Command {

    public static final String COMMAND_WORD = "deleteclass";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the academic class with the specified module code and class name. "
            + "Parameters: "
            + PREFIX_MODULE_CODE + "MODULE_CODE "
            + PREFIX_CLASS_NAME + "CLASS_NAME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_MODULE_CODE + "CS2103 "
            + PREFIX_CLASS_NAME + "F10-2";

    public static final String MESSAGE_DELETE_CLASS_SUCCESS = "Deleted class: %1$s";
    public static final String MESSAGE_CLASS_NOT_FOUND =
            "No class with module code %1$s and class name %2$s exists.";

    private static final Logger logger = LogsCenter.getLogger(DeleteClassCommand.class);

    private final ModuleCode moduleCode;
    private final ClassName className;

    /**
     * Creates a command to delete the class matching the given module code and class name.
     */
    public DeleteClassCommand(ModuleCode moduleCode, ClassName className) {
        requireAllNonNull(moduleCode, className);
        this.moduleCode = moduleCode;
        this.className = className;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        AcademicClass classToDelete = model.getAddressBook().findAcademicClass(moduleCode, className)
                .orElseThrow(() -> {
                    logger.fine("Class deletion rejected: specified class does not exist.");
                    return new CommandException(
                            String.format(MESSAGE_CLASS_NOT_FOUND, moduleCode, className));
                });

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

        return moduleCode.equals(otherDeleteClassCommand.moduleCode)
                && className.equals(otherDeleteClassCommand.className);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("moduleCode", moduleCode)
                .add("className", className)
                .toString();
    }
}
