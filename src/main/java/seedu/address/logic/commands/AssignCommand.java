package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/** Assigns an existing student to an existing class. */
public class AssignCommand extends Command {
    public static final String COMMAND_WORD = "assign";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Assigns a student to a class.\n"
            + "Parameters: sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME\n"
            + "Example: assign sn/A1111111H m/CS2103 c/F10";
    public static final String MESSAGE_SUCCESS = "Student (%1$s) has been assigned to %2$s.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student (%1$s) cannot be found in the address book.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "Class (%1$s-%2$s) cannot be found in the address book.";

    private final String studentNumber;
    private final String moduleCode;
    private final String className;

    /** Creates a command with search values already trimmed by the parser. */
    public AssignCommand(String studentNumber, String moduleCode, String className) {
        requireAllNonNull(studentNumber, moduleCode, className);
        this.studentNumber = studentNumber;
        this.moduleCode = moduleCode;
        this.className = className;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        ReadOnlyAddressBook addressBook = model.getAddressBook();
        if (!StudentNumber.isValidStudentNumber(studentNumber)) {
            throw new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentNumber));
        }
        Person person = addressBook.findPersonByStudentNumber(new StudentNumber(studentNumber))
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentNumber)));
        if (!ModuleCode.isValidModuleCode(moduleCode) || !ClassName.isValidClassName(className)) {
            throw new CommandException(String.format(MESSAGE_CLASS_NOT_FOUND, moduleCode, className));
        }
        AcademicClass academicClass = addressBook
                .findAcademicClass(new ModuleCode(moduleCode), new ClassName(className))
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_CLASS_NOT_FOUND, moduleCode, className)));
        model.assignStudentToClass(person, academicClass);
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getStudentNumber(), academicClass));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AssignCommand otherCommand)) {
            return false;
        }
        return studentNumber.equals(otherCommand.studentNumber)
                && moduleCode.equals(otherCommand.moduleCode) && className.equals(otherCommand.className);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentNumber", studentNumber)
                .add("moduleCode", moduleCode).add("className", className).toString();
    }
}
