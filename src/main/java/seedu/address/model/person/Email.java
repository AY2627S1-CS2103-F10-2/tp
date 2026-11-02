package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final int MAX_LENGTH = 200;
    private static final String SPECIAL_CHARACTERS = "+_.-";
    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and be at most " + MAX_LENGTH + " characters long.\n"
            + "They must adhere to the following constraints:\n"
            + "1. The local-part may contain only letters, digits, and these special characters: "
            + SPECIAL_CHARACTERS + "\n"
            + "   It must start and end with a letter or digit.\n"
            + "   Special characters must be separated by at least one letter or digit.\n"
            + "2. The local-part is followed by an '@' and then a domain name.\n"
            + "   The domain name must:\n"
            + "   - contain at least one period separating its labels\n"
            + "   - end with a label at least 2 characters long\n"
            + "   - have each label start and end with a letter or digit\n"
            + "   - contain only letters, digits, and hyphens within each label\n"
            + "   - have hyphens separated by at least one letter or digit";
    // alphanumeric and special characters
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[^\\W_]+"; // alphanumeric characters except underscore
    private static final String LOCAL_PART_REGEX = "^" + ALPHANUMERIC_NO_UNDERSCORE + "([" + SPECIAL_CHARACTERS + "]"
            + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_LAST_PART_REGEX = "(?=.{2,}$)" + DOMAIN_PART_REGEX + "$"; // At least two chars
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)+" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        // Length is checked first to avoid regex backtracking causing a StackOverflowError on very long input
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
