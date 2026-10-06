package seedu.address.model.academicclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ModuleCodeTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ModuleCode(null));
    }

    @Test
    public void constructor_invalidModuleCode_throwsIllegalArgumentException() {
        String invalidModuleCode = "";
        assertThrows(IllegalArgumentException.class, () -> new ModuleCode(invalidModuleCode));
    }

    @Test
    public void constructor_validModuleCode_convertsToUpperCase() {
        assertEquals("CS2103T", new ModuleCode(" cs2103t ").value);
    }

    @Test
    public void isValidModuleCode() {
        // null module code
        assertThrows(NullPointerException.class, () -> ModuleCode.isValidModuleCode(null));

        // invalid module codes
        assertFalse(ModuleCode.isValidModuleCode("")); // empty string
        assertFalse(ModuleCode.isValidModuleCode(" ")); // spaces only
        assertFalse(ModuleCode.isValidModuleCode("2103")); // does not begin with alphabet
        assertFalse(ModuleCode.isValidModuleCode("CS 2103")); // contains space
        assertFalse(ModuleCode.isValidModuleCode("CS-2103")); // contains hyphen
        assertFalse(ModuleCode.isValidModuleCode("CS_2103")); // contains underscore

        // valid module codes
        assertTrue(ModuleCode.isValidModuleCode("CS2103")); // alphabets and numbers
        assertTrue(ModuleCode.isValidModuleCode("cs2103t")); // lowercase alphabets
        assertTrue(ModuleCode.isValidModuleCode("A1234")); // begins with alphabet
        assertTrue(ModuleCode.isValidModuleCode("A")); // one character
    }

    @Test
    public void equals() {
        ModuleCode moduleCode = new ModuleCode("CS2103T");

        // same values -> returns true
        assertTrue(moduleCode.equals(new ModuleCode("CS2103T")));

        // same normalized values -> returns true
        assertTrue(moduleCode.equals(new ModuleCode(" cs2103t ")));

        // same object -> returns true
        assertTrue(moduleCode.equals(moduleCode));

        // null -> returns false
        assertFalse(moduleCode.equals(null));

        // different types -> returns false
        assertFalse(moduleCode.equals(5.0f));

        // different values -> returns false
        assertFalse(moduleCode.equals(new ModuleCode("ST2334")));
    }
}
