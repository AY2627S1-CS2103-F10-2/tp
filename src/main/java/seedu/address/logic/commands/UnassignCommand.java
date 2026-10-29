package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.person.Person;

/**
 * Unassigns a student from a class using indices in their respective displayed lists.
 */
public class UnassignCommand extends Command {

    public static final String COMMAND_WORD = "unassign";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Unassigns a student from a class.\n"
            + "Parameters: STUDENT_INDEX CLASS_INDEX (positive integers in their respective displayed lists)\n"
            + "Example: unassign 1 2";
    public static final String MESSAGE_SUCCESS = "Student (%1$s) has been unassigned from %2$s.";

    private final Index studentIndex;
    private final Index classIndex;

    /**
     * Creates a command with the parsed student and class indices.
     */
    public UnassignCommand(Index studentIndex, Index classIndex) {
        this.studentIndex = studentIndex;
        this.classIndex = classIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> displayedStudents = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= displayedStudents.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = displayedStudents.get(studentIndex.getZeroBased());

        AcademicClass academicClass = getAcademicClass(model);

        if (!model.unassignStudentFromClass(person, academicClass)) {
            throw new CommandException(String.format(Messages.MESSAGE_STUDENT_NOT_IN_CLASS,
                    person.getStudentNumber(), academicClass));
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getStudentNumber(), academicClass));
    }

    /** Resolves the displayed class after validating its index. */
    private AcademicClass getAcademicClass(Model model) throws CommandException {
        List<AcademicClass> displayedClasses = model.getFilteredAcademicClassList();
        if (classIndex.getZeroBased() >= displayedClasses.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        }

        return displayedClasses.get(classIndex.getZeroBased());
    }
}
