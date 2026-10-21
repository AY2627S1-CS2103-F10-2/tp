package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;

/** Removes one recorded attendance date. */
public class DeleteAttendanceCommand extends Command {
    public static final String COMMAND_WORD = "deleteattendance";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes a recorded attendance date. "
            + "Parameters: INDEX (a positive integer in the displayed student list) "
            + "m/MODULE_CODE c/CLASS_NAME d/YYYY-MM-DD\n"
            + "Example: " + COMMAND_WORD + " 1 m/CS2103 c/F10-2 d/2024-02-29";
    public static final String MESSAGE_SUCCESS = "Removed attendance for student (%1$s) in %2$s on %3$s.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "Class (%1$s-%2$s) cannot be found in the address book.";
    public static final String MESSAGE_ATTENDANCE_NOT_FOUND =
            "Attendance for student (%1$s) in %2$s on %3$s is not recorded.";

    private final Index index;
    private final ModuleCode moduleCode;
    private final ClassName className;
    private final LocalDate date;

    /** Creates a deletion command with validated identifiers and a parsed date. */
    public DeleteAttendanceCommand(Index index, ModuleCode moduleCode,
            ClassName className, LocalDate date) {
        requireAllNonNull(index, moduleCode, className, date);
        this.index = index;
        this.moduleCode = moduleCode;
        this.className = className;
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedStudents = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedStudents.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = displayedStudents.get(index.getZeroBased());
        AcademicClass academicClass = model.findAcademicClass(moduleCode, className)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_CLASS_NOT_FOUND, moduleCode, className)));
        removeAttendance(model, person, academicClass);
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getStudentNumber(), academicClass, date));
    }

    private void removeAttendance(Model model, Person person, AcademicClass academicClass) throws CommandException {
        boolean isRemoved;
        try {
            // Retained history can be corrected even when the student is no longer assigned.
            isRemoved = model.removeAttendance(person, academicClass, date);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        if (!isRemoved) {
            throw new CommandException(String.format(MESSAGE_ATTENDANCE_NOT_FOUND,
                    person.getStudentNumber(), academicClass, date));
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DeleteAttendanceCommand otherCommand)) {
            return false;
        }
        return index.equals(otherCommand.index)
                && moduleCode.equals(otherCommand.moduleCode)
                && className.equals(otherCommand.className)
                && date.equals(otherCommand.date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index)
                .add("moduleCode", moduleCode).add("className", className).add("date", date).toString();
    }
}
