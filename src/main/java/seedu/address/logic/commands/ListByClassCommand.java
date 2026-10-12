package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Locale;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;

/**
 * Lists students assigned to a specified academic class.
 */
public class ListByClassCommand extends Command {
    public static final String COMMAND_WORD = "listbyclass";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists students assigned to a class alphabetically. "
            + "Parameters: m/MODULE_CODE c/CLASS_NAME\n"
            + "Example: " + COMMAND_WORD + " m/CS2103 c/F10-2";
    public static final String MESSAGE_CLASS_NOT_FOUND = "The specified class does not exist.";
    public static final String MESSAGE_SUCCESS = "Listed %1$d students taking %2$s (%3$s).";

    private final String moduleCode;
    private final String className;

    /**
     * Creates a command to look up the specified module and class, ignoring case and surrounding spaces.
     */
    public ListByClassCommand(String moduleCode, String className) {
        requireAllNonNull(moduleCode, className);
        checkArgument(!moduleCode.trim().isEmpty(), "Module code must not be blank.");
        checkArgument(!className.trim().isEmpty(), "Class name must not be blank.");
        this.moduleCode = moduleCode.trim().toUpperCase(Locale.ROOT);
        this.className = className.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        AcademicClass academicClass = findAcademicClass(model);
        model.updateFilteredPersonListAlphabetically(person -> academicClass.getStudents().stream()
                .anyMatch(member -> member.isSamePerson(person)));
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getFilteredPersonList().size(),
                academicClass.getModuleCode(), academicClass.getClassName()));
    }

    private AcademicClass findAcademicClass(Model model) throws CommandException {
        return model.getAcademicClassList().stream()
                .filter(academicClass -> academicClass.getModuleCode().value.equalsIgnoreCase(moduleCode)
                        && academicClass.getClassName().value.equalsIgnoreCase(className))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_CLASS_NOT_FOUND));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ListByClassCommand otherCommand)) {
            return false;
        }
        return moduleCode.equals(otherCommand.moduleCode) && className.equals(otherCommand.className);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("moduleCode", moduleCode)
                .add("className", className)
                .toString();
    }
}
