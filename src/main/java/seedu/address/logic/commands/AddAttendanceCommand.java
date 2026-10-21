package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/** Records one attended date for a student currently assigned to a class. */
public class AddAttendanceCommand extends Command {
    public static final String COMMAND_WORD = "addattendance";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Records attendance for an assigned student. "
            + "Parameters: sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME d/YYYY-MM-DD\n"
            + "Example: " + COMMAND_WORD + " sn/A0123456X m/CS2103 c/F10-2 d/2024-02-29";
    public static final String MESSAGE_SUCCESS = "Recorded attendance for student (%1$s) in %2$s on %3$s.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student (%1$s) cannot be found in the address book.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "Class (%1$s-%2$s) cannot be found in the address book.";
    public static final String MESSAGE_NOT_ASSIGNED = "Student (%1$s) is not assigned to class %2$s.";
    public static final String MESSAGE_DUPLICATE_ATTENDANCE =
            "Attendance for student (%1$s) in %2$s on %3$s is already recorded.";

    private final StudentNumber studentNumber;
    private final ModuleCode moduleCode;
    private final ClassName className;
    private final LocalDate date;

    /**
     * Creates a command with validated identifiers and a parsed date.
     * Date eligibility is checked by the model when the command executes.
     */
    public AddAttendanceCommand(StudentNumber studentNumber, ModuleCode moduleCode,
            ClassName className, LocalDate date) {
        requireAllNonNull(studentNumber, moduleCode, className, date);
        this.studentNumber = studentNumber;
        this.moduleCode = moduleCode;
        this.className = className;
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person person = model.findPersonByStudentNumber(studentNumber)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentNumber)));
        AcademicClass academicClass = model.findAcademicClass(moduleCode, className)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_CLASS_NOT_FOUND, moduleCode, className)));
        if (!academicClass.getStudents().contains(person)) {
            throw new CommandException(String.format(MESSAGE_NOT_ASSIGNED, studentNumber, academicClass));
        }
        recordAttendance(model, person, academicClass);
        return new CommandResult(String.format(MESSAGE_SUCCESS, studentNumber, academicClass, date));
    }

    private void recordAttendance(Model model, Person person, AcademicClass academicClass) throws CommandException {
        boolean isAdded;
        try {
            isAdded = model.addAttendance(person, academicClass, date);
        } catch (IllegalArgumentException e) {
            // Keep date validation in the model, but report invalid input as a command failure.
            throw new CommandException(e.getMessage(), e);
        }
        if (!isAdded) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_ATTENDANCE,
                    studentNumber, academicClass, date));
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AddAttendanceCommand otherCommand)) {
            return false;
        }
        return studentNumber.equals(otherCommand.studentNumber)
                && moduleCode.equals(otherCommand.moduleCode)
                && className.equals(otherCommand.className)
                && date.equals(otherCommand.date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentNumber", studentNumber)
                .add("moduleCode", moduleCode).add("className", className).add("date", date).toString();
    }
}
