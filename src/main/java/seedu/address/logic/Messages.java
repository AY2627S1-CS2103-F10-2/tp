package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND =
            "Unknown command '%1$s'. Command names are case-sensitive. Type help for usage instructions.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_STUDENT_NUMBER =
            "A student with this student number already exists.";
    public static final String MESSAGE_DUPLICATE_EMAIL = "A student with this email already exists.";
    public static final String MESSAGE_DUPLICATE_TELEGRAM = "A student with this Telegram username already exists.";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    public static final String MESSAGE_STUDENT_NOT_IN_CLASS = "Student (%1$s) is not assigned to class %2$s.";

    /** Describes the valid indices in the currently displayed Students list. */
    public static String getInvalidStudentIndexMessage(Index index, int displayedCount) {
        if (displayedCount == 0) {
            return "No students are displayed. Use list to show all students, or add a student.";
        }
        return String.format("Student index %1$d is invalid for the current Students list. "
                + "Choose an index from 1 to %2$d, or use list to show all students.",
                index.getOneBased(), displayedCount);
    }

    /** Describes the valid indices in the currently displayed Classes list. */
    public static String getInvalidClassIndexMessage(Index index, int displayedCount) {
        if (displayedCount == 0) {
            return "No classes are displayed. Use listclass to show all classes, or add a class with addclass.";
        }
        return String.format("Class index %1$d is invalid for the current Classes list. "
                + "Choose an index from 1 to %2$d, or use listclass to show all classes.",
                index.getOneBased(), displayedCount);
    }

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Student Number: ")
                .append(person.getStudentNumber())
                .append("; Email: ")
                .append(person.getEmail());
        person.getTelegram().ifPresent(telegram -> builder.append("; Telegram: @").append(telegram));
        if (!person.getTags().isEmpty()) {
            builder.append("; Tags: ");
            person.getTags().forEach(builder::append);
        }
        return builder.toString();
    }

}
