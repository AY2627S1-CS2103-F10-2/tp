package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TelegramTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Telegram(null));
    }

    @Test
    public void constructor_invalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Telegram.MESSAGE_CONSTRAINTS, () -> new Telegram("@abc"));
    }

    @Test
    public void constructor_leadingAt_stripped() {
        assertEquals("alice_tan", new Telegram("@alice_tan").value);
        assertEquals("alice_tan", new Telegram("alice_tan").value);
    }

    @Test
    public void isValidTelegram() {
        assertThrows(NullPointerException.class, () -> Telegram.isValidTelegram(null));

        // invalid
        assertFalse(Telegram.isValidTelegram("")); // empty
        assertFalse(Telegram.isValidTelegram("@")); // only '@'
        assertFalse(Telegram.isValidTelegram("@abc")); // too short
        assertFalse(Telegram.isValidTelegram("abcd")); // too short
        assertFalse(Telegram.isValidTelegram("a".repeat(33))); // too long
        assertFalse(Telegram.isValidTelegram("@" + "a".repeat(33))); // too long with '@'
        assertFalse(Telegram.isValidTelegram("al ice")); // space
        assertFalse(Telegram.isValidTelegram("alice-tan")); // hyphen
        assertFalse(Telegram.isValidTelegram("@@alice")); // double '@'
        assertFalse(Telegram.isValidTelegram("ali@ce")); // '@' not at start

        // valid
        assertTrue(Telegram.isValidTelegram("alice_tan"));
        assertTrue(Telegram.isValidTelegram("@alice_tan"));
        assertTrue(Telegram.isValidTelegram("student123"));
        assertTrue(Telegram.isValidTelegram("abcde")); // minimum length
        assertTrue(Telegram.isValidTelegram("a".repeat(32))); // maximum length
        assertTrue(Telegram.isValidTelegram("@" + "a".repeat(32))); // maximum length with '@'
        assertTrue(Telegram.isValidTelegram("_____")); // underscores only
    }

    @Test
    public void equals() {
        Telegram telegram = new Telegram("alice_tan");

        // same values -> returns true
        assertTrue(telegram.equals(new Telegram("alice_tan")));

        // with and without '@' -> returns true
        assertTrue(telegram.equals(new Telegram("@alice_tan")));

        // different case -> returns true
        assertTrue(telegram.equals(new Telegram("Alice_Tan")));
        assertEquals(telegram.hashCode(), new Telegram("Alice_Tan").hashCode());

        // same object -> returns true
        assertTrue(telegram.equals(telegram));

        // null -> returns false
        assertFalse(telegram.equals(null));

        // different types -> returns false
        assertFalse(telegram.equals(5.0f));

        // different values -> returns false
        assertFalse(telegram.equals(new Telegram("chloe_ng")));
    }
}
