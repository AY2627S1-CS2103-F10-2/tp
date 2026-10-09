package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Person's student number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidStudentNumber(String)}.
 * The value is stored in uppercase, so comparison is case-insensitive.
 */
public class StudentNumber {

    public static final String MESSAGE_EMPTY = "Student number cannot be empty.";
    public static final String MESSAGE_CONSTRAINTS =
            "Invalid student number. Student numbers must contain 5–20 letters or digits with no spaces.";

    public static final String VALIDATION_REGEX = "\\p{Alnum}{5,20}";

    public final String value;

    /**
     * Constructs a {@code StudentNumber}.
     *
     * @param studentNumber A valid student number.
     */
    public StudentNumber(String studentNumber) {
        requireNonNull(studentNumber);
        checkArgument(!studentNumber.isEmpty(), MESSAGE_EMPTY);
        checkArgument(isValidStudentNumber(studentNumber), MESSAGE_CONSTRAINTS);
        value = studentNumber.toUpperCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string is a valid student number.
     */
    public static boolean isValidStudentNumber(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof StudentNumber otherStudentNumber)) {
            return false;
        }

        return value.equals(otherStudentNumber.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
