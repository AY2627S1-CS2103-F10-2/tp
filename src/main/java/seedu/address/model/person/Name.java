package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must contain at least one letter (A-Z, a-z) or digit (0-9). Spaces are allowed.\n"
            + "Allowed punctuation: hyphen (-), straight apostrophe ('), period (.), comma (,), "
            + "parentheses ( ), and forward slash (/).\n"
            + "Stored names must use / rather than \\. "
            + "Non-ASCII characters and other punctuation are not allowed.";

    /*
     * Stored names use forward slashes. The command parsers convert backslashes only after extracting the name.
     * Require an ASCII letter or digit somewhere, and disallow leading spaces as before.
     */
    public static final String VALIDATION_REGEX = "(?=.*[A-Za-z0-9])[A-Za-z0-9'.,()/-][A-Za-z0-9'.,()/ -]*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name, with forward slashes rather than command-input backslashes.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
