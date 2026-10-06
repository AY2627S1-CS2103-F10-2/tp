package seedu.address.model.academicclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ClassNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ClassName(null));
    }

    @Test
    public void constructor_invalidClassName_throwsIllegalArgumentException() {
        String invalidClassName = "";
        assertThrows(IllegalArgumentException.class, () -> new ClassName(invalidClassName));
    }

    @Test
    public void constructor_validClassName_convertsToUpperCase() {
        assertEquals("F10-2", new ClassName(" f10-2 ").value);
    }

    @Test
    public void isValidClassName() {
        // null class name
        assertThrows(NullPointerException.class, () -> ClassName.isValidClassName(null));

        // invalid class names
        assertFalse(ClassName.isValidClassName("")); // empty string
        assertFalse(ClassName.isValidClassName(" ")); // spaces only
        assertFalse(ClassName.isValidClassName("---")); // no alphanumeric character
        assertFalse(ClassName.isValidClassName("T 24")); // spaces within class name
        assertFalse(ClassName.isValidClassName("T/24")); // contains slash
        assertFalse(ClassName.isValidClassName("T_24")); // contains underscore

        // valid class names
        assertTrue(ClassName.isValidClassName("F10")); // alphabets and numbers
        assertTrue(ClassName.isValidClassName("F10-2")); // with hyphen
        assertTrue(ClassName.isValidClassName("24")); // numbers only
        assertTrue(ClassName.isValidClassName("T")); // one character
    }

    @Test
    public void equals() {
        ClassName className = new ClassName("F10-2");

        // same values -> returns true
        assertTrue(className.equals(new ClassName("F10-2")));

        // same normalized values -> returns true
        assertTrue(className.equals(new ClassName(" f10-2 ")));

        // same object -> returns true
        assertTrue(className.equals(className));

        // null -> returns false
        assertFalse(className.equals(null));

        // different types -> returns false
        assertFalse(className.equals(5.0f));

        // different values -> returns false
        assertFalse(className.equals(new ClassName("T24")));
    }
}
