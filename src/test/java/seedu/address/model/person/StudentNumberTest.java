package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StudentNumberTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StudentNumber(null));
    }

    @Test
    public void constructor_empty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, StudentNumber.MESSAGE_EMPTY, () -> new StudentNumber(""));
    }

    @Test
    public void constructor_invalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, StudentNumber.MESSAGE_CONSTRAINTS, () -> new StudentNumber("ABC"));
    }

    @Test
    public void constructor_lowercase_storedAsUppercase() {
        assertEquals("A0123456X", new StudentNumber("a0123456x").value);
    }

    @Test
    public void isValidStudentNumber() {
        assertThrows(NullPointerException.class, () -> StudentNumber.isValidStudentNumber(null));

        // invalid
        assertFalse(StudentNumber.isValidStudentNumber("")); // empty
        assertFalse(StudentNumber.isValidStudentNumber("ABC")); // too short
        assertFalse(StudentNumber.isValidStudentNumber("A123")); // 4 characters
        assertFalse(StudentNumber.isValidStudentNumber("A".repeat(21))); // too long
        assertFalse(StudentNumber.isValidStudentNumber("A01 23456X")); // space
        assertFalse(StudentNumber.isValidStudentNumber(" A0123456X")); // leading space
        assertFalse(StudentNumber.isValidStudentNumber("A01234@6X")); // special character
        assertFalse(StudentNumber.isValidStudentNumber("A0123_56X")); // underscore

        // valid
        assertTrue(StudentNumber.isValidStudentNumber("A0123456X"));
        assertTrue(StudentNumber.isValidStudentNumber("a0123456x")); // lowercase
        assertTrue(StudentNumber.isValidStudentNumber("A1234")); // minimum length
        assertTrue(StudentNumber.isValidStudentNumber("A".repeat(20))); // maximum length
        assertTrue(StudentNumber.isValidStudentNumber("12345")); // digits only
    }

    @Test
    public void equals() {
        StudentNumber studentNumber = new StudentNumber("A0123456X");

        // same values -> returns true
        assertTrue(studentNumber.equals(new StudentNumber("A0123456X")));

        // same values, different case -> returns true
        assertTrue(studentNumber.equals(new StudentNumber("a0123456x")));
        assertEquals(studentNumber.hashCode(), new StudentNumber("a0123456x").hashCode());

        // same object -> returns true
        assertTrue(studentNumber.equals(studentNumber));

        // null -> returns false
        assertFalse(studentNumber.equals(null));

        // different types -> returns false
        assertFalse(studentNumber.equals(5.0f));

        // different values -> returns false
        assertFalse(studentNumber.equals(new StudentNumber("A0234567Y")));
    }
}
