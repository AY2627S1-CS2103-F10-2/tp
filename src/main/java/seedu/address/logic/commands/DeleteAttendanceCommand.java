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
import seedu.address.model.person.Person;

/** Removes one recorded attendance date. */
public class DeleteAttendanceCommand extends Command {
    public static final String COMMAND_WORD = "deleteattendance";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes a recorded attendance date. "
            + "Parameters: STUDENT_INDEX CLASS_INDEX "
            + "(whole numbers from 1 to 2147483647 in their respective displayed lists) "
            + "d/YYYY-MM-DD\n"
            + "Example: " + COMMAND_WORD + " 1 2 d/2024-02-29";
    public static final String MESSAGE_SUCCESS = "Removed attendance for student (%1$s) in %2$s on %3$s.";
    public static final String MESSAGE_ATTENDANCE_NOT_FOUND =
            "Attendance for student (%1$s) in %2$s on %3$s is not recorded.";

    private final Index index;
    private final Index classIndex;
    private final LocalDate date;

    /** Creates a deletion command with validated indices and a parsed date. */
    public DeleteAttendanceCommand(Index index, Index classIndex, LocalDate date) {
        requireAllNonNull(index, classIndex, date);
        this.index = index;
        this.classIndex = classIndex;
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedStudents = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedStudents.size()) {
            throw new CommandException(Messages.getInvalidStudentIndexMessage(
                    index, model.getFilteredPersonList().size()));
        }
        Person person = displayedStudents.get(index.getZeroBased());
        List<AcademicClass> displayedClasses = model.getFilteredAcademicClassList();
        if (classIndex.getZeroBased() >= displayedClasses.size()) {
            throw new CommandException(Messages.getInvalidClassIndexMessage(
                    classIndex, model.getFilteredAcademicClassList().size()));
        }
        AcademicClass academicClass = displayedClasses.get(classIndex.getZeroBased());
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
                && classIndex.equals(otherCommand.classIndex)
                && date.equals(otherCommand.date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index)
                .add("classIndex", classIndex).add("date", date).toString();
    }
}
