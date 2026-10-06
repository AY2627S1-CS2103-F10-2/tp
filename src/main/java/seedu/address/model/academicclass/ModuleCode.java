package seedu.address.model.academicclass;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Class's module code in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidModuleCode(String)}
 */
public class ModuleCode {

    public static final String MESSAGE_EMPTY = "Module code cannot be empty";
    public static final String MESSAGE_CONSTRAINTS = "Module code must contain letters or digits";

    /*
     * The first character of the module code must be an alphabet,
     * and all remaining characters must be alphanumeric.
     */
    public static final String VALIDATION_REGEX = "\\p{Alpha}\\p{Alnum}*";

    public final String value;

    /**
     * Constructs a {@code ModuleCode}.
     *
     * @param moduleCode A valid module code.
     */
    public ModuleCode(String moduleCode) {
        requireNonNull(moduleCode);
        String trimmedModuleCode = moduleCode.trim();
        checkArgument(isValidModuleCode(trimmedModuleCode), MESSAGE_CONSTRAINTS);
        value = trimmedModuleCode.toUpperCase(Locale.ENGLISH);
    }

    /**
     * Returns true if a given string is a valid module code.
     */
    public static boolean isValidModuleCode(String test) {
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
        if (!(other instanceof ModuleCode otherModuleCode)) {
            return false;
        }

        return value.equals(otherModuleCode.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
