package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.person.Person;

/**
 * Assigns an existing student to an existing class.
 */
public class AssignCommand extends Command {
    public static final String COMMAND_WORD = "assign";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Assigns a student to a class.\n"
            + "Parameters: STUDENT_INDEX CLASS_INDEX (non-zero positive integers in their respective displayed lists)\n"
            + "Example: assign 1 2";
    public static final String MESSAGE_SUCCESS = "Student (%1$s) has been assigned to %2$s.";
    public static final String MESSAGE_ALREADY_ASSIGNED = "Student (%1$s) is already assigned to class %2$s.";

    private final Index studentIndex;
    private final Index classIndex;

    /** Creates a command with validated displayed student and class indices. */
    public AssignCommand(Index studentIndex, Index classIndex) {
        requireAllNonNull(studentIndex, classIndex);
        this.studentIndex = studentIndex;
        this.classIndex = classIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person person = model.findDisplayedPerson(studentIndex)
                .orElseThrow(() -> new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX));
        AcademicClass academicClass = model.findDisplayedAcademicClass(classIndex)
                .orElseThrow(() -> new CommandException(Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX));
        if (!model.assignStudentToClass(person, academicClass)) {
            throw new CommandException(String.format(MESSAGE_ALREADY_ASSIGNED,
                    person.getStudentNumber(), academicClass));
        }
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
        return studentIndex.equals(otherCommand.studentIndex) && classIndex.equals(otherCommand.classIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex)
                .add("classIndex", classIndex).toString();
    }
}
