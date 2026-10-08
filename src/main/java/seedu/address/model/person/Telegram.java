package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Person's Telegram username in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidTelegram(String)}.
 * The value is stored without a leading '@'. Comparison is case-insensitive.
 */
public class Telegram {

    public static final String MESSAGE_CONSTRAINTS =
            "Invalid Telegram username. Usernames must contain 5–32 letters, digits, or underscores.";

    public static final String VALIDATION_REGEX = "@?\\w{5,32}";

    public final String value;

    /**
     * Constructs a {@code Telegram}.
     *
     * @param telegram A valid Telegram username, with or without a leading '@'.
     */
    public Telegram(String telegram) {
        requireNonNull(telegram);
        checkArgument(isValidTelegram(telegram), MESSAGE_CONSTRAINTS);
        value = telegram.startsWith("@") ? telegram.substring(1) : telegram;
    }

    /**
     * Returns true if a given string is a valid Telegram username.
     */
    public static boolean isValidTelegram(String test) {
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
        if (!(other instanceof Telegram otherTelegram)) {
            return false;
        }

        return value.equalsIgnoreCase(otherTelegram.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase(Locale.ROOT).hashCode();
    }

}
