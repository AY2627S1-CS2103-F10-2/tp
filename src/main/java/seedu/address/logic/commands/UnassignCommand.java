package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/**
 * Unassigns an existing student from an existing academic class.
 */
public class UnassignCommand extends Command {

    public static final String COMMAND_WORD = "unassign";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Unassigns a student from a class.\n"
            + "Parameters: sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME\n"
            + "Example: unassign sn/A1111111H m/CS2103 c/F10";
    public static final String MESSAGE_SUCCESS = "Student (%1$s) has been unassigned from %2$s.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student (%1$s) cannot be found in the address book.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "Class (%1$s-%2$s) cannot be found in the address book.";

    private final StudentNumber studentNumber;
    private final ModuleCode moduleCode;
    private final ClassName className;

    /**
     * Creates a command with the given student number, module code and class name.
     */
    public UnassignCommand(StudentNumber studentNumber, ModuleCode moduleCode, ClassName className) {
        this.studentNumber = studentNumber;
        this.moduleCode = moduleCode;
        this.className = className;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        ReadOnlyAddressBook addressBook = model.getAddressBook();
        Person person = addressBook.findPersonByStudentNumber(studentNumber)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentNumber)));
        AcademicClass academicClass = addressBook
                .findAcademicClass(moduleCode, className)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_CLASS_NOT_FOUND, moduleCode, className)));

        if (!model.unassignStudentFromClass(person, academicClass)) {
            throw new CommandException("Student is not assigned to this class.");
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getStudentNumber(), academicClass));
    }
}
