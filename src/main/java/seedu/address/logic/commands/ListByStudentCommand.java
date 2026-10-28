package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.person.Person;

/** Lists the academic classes assigned to a student identified by their displayed index. */
public class ListByStudentCommand extends Command {

    public static final String COMMAND_WORD = "listbystudent";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists classes assigned to the student at the displayed index.\n"
            + "Parameters: INDEX (whole number from 1 to 2147483647)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Listed %1$d classes for %2$s (%3$s).";
    public static final String MESSAGE_SINGLE_CLASS_SUCCESS = "Listed 1 class for %1$s (%2$s).";

    private static final Logger logger = LogsCenter.getLogger(ListByStudentCommand.class);

    private final Index targetIndex;

    /** Creates a command targeting the student at {@code targetIndex} in the displayed list. */
    public ListByStudentCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedStudents = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedStudents.size()) {
            logger.fine(() -> "Rejected listbystudent index " + targetIndex.getOneBased()
                    + " for a displayed student list of size " + displayedStudents.size());
            throw new CommandException(Messages.getInvalidStudentIndexMessage(
                    targetIndex, model.getFilteredPersonList().size()));
        }

        Person student = displayedStudents.get(targetIndex.getZeroBased());
        model.updateFilteredAcademicClassListByStudent(student.getStudentNumber());
        List<AcademicClass> matchingClasses = model.getFilteredAcademicClassList();
        // This is a model postcondition, not validation of user input.
        assert matchingClasses.stream().allMatch(academicClass -> academicClass.getStudents().stream()
                .anyMatch(member -> member.isSamePerson(student)))
                : "The class filter returned a class without the selected student";
        logger.fine(() -> "Listed " + matchingClasses.size() + " classes for student " + student.getStudentNumber()
                + " at displayed index " + targetIndex.getOneBased());
        String feedback = matchingClasses.size() == 1
                ? String.format(MESSAGE_SINGLE_CLASS_SUCCESS, student.getName(), student.getStudentNumber())
                : String.format(MESSAGE_SUCCESS, matchingClasses.size(), student.getName(), student.getStudentNumber());
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof ListByStudentCommand otherCommand && targetIndex.equals(otherCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
