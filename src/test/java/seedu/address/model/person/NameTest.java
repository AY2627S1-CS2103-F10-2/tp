package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
    }

    @Test
    public void isValidName_allowedPunctuation_acceptsStoredNames() {
        for (String name : new String[] {"Anne-Marie Tan", "Sean O'Connor", "J. K. Tan", "John Tan, Jr.",
            "Tan Wei Ming (Alex)", "Ravi S/O Kumar", "'t Hooft", "(Alex) Tan", "A/B-C'D.E,F(G)"}) {
            assertTrue(Name.isValidName(name), name);
        }
    }

    @Test
    public void isValidName_disallowedCharactersOrPunctuationOnly_rejectsName() {
        for (String name : new String[] {"---", "()", " . , '/- ", "Sean O\u2019Connor", "Anne\u2013Marie",
            "Anne\u2014Marie", "Anne\u2011Marie", "Jos\u00e9", "Jose\u0301", "\u738b\u660e",
            "\u0645\u062d\u0645\u062f", "Marcel\u00b7li", "Alex_Name", "Alex@Tan", "Alex+Tan", "Alex&Tan",
            "Alex\tTan", "Alex\nTan", "Alex\u00a0Tan", "Alex\ud83d\ude00", "Ravi S\\O Kumar", " Alex"}) {
            assertFalse(Name.isValidName(name), name);
        }
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
