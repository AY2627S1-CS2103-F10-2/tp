package seedu.address.model.academicclass;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Class's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidClassName(String)}
 */
public class ClassName {

    public static final String MESSAGE_EMPTY = "Class name cannot be empty";
    public static final String MESSAGE_CONSTRAINTS = "Class names can only contain letters, digits, or '-'";

    /*
     * The class name must contain at least one alphanumeric character,
     * otherwise "---" becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "(?=.*[\\p{Alnum}])[\\p{Alnum}-]+";

    public final String value;

    /**
     * Constructs a {@code ClassName}.
     *
     * @param className A valid class name.
     */
    public ClassName(String className) {
        requireNonNull(className);
        String trimmedClassName = className.trim();
        checkArgument(isValidClassName(trimmedClassName), MESSAGE_CONSTRAINTS);
        value = trimmedClassName.toUpperCase(Locale.ENGLISH);
    }

    /**
     * Returns true if a given string is a valid class name.
     */
    public static boolean isValidClassName(String test) {
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
        if (!(other instanceof ClassName otherClassName)) {
            return false;
        }

        return value.equals(otherClassName.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
