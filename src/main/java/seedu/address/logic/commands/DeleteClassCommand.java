package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import java.util.Locale;

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

    private final String moduleCode;
    private final String className;

    /**
     * Creates a command to delete the class matching the given nonempty lookup values.
     * Unlike class creation, lookup does not impose module code or class name format constraints.
     */
    public DeleteClassCommand(String moduleCode, String className) {
        requireAllNonNull(moduleCode, className);
        checkArgument(!moduleCode.trim().isEmpty(), ModuleCode.MESSAGE_EMPTY);
        checkArgument(!className.trim().isEmpty(), ClassName.MESSAGE_EMPTY);
        this.moduleCode = moduleCode.trim().toUpperCase(Locale.ENGLISH);
        this.className = className.trim().toUpperCase(Locale.ENGLISH);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        AcademicClass classToDelete = model.getAddressBook().getAcademicClassList().stream()
                .filter(academicClass -> academicClass.getModuleCode().value.equals(moduleCode)
                        && academicClass.getClassName().value.equals(className))
                .findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_CLASS_NOT_FOUND, moduleCode, className)));

        model.deleteAcademicClass(classToDelete);
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
