package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditClassCommand.EditClassDescriptor;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Tests optional edits and independent descriptor copies.
 */
public class EditClassDescriptorTest {
    @Test
    public void isAnyFieldEdited_checksBothOptionalFields() {
        EditClassDescriptor descriptor = new EditClassDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        assertTrue(descriptor.getClassName().isEmpty());
        assertTrue(descriptor.getModuleCode().isEmpty());
        descriptor.setClassName(new ClassName("OB1"));
        assertTrue(descriptor.isAnyFieldEdited());
        descriptor.setClassName(null);
        descriptor.setModuleCode(new ModuleCode("CS2103"));
        assertTrue(descriptor.isAnyFieldEdited());
        descriptor.setModuleCode(null);
        assertFalse(descriptor.isAnyFieldEdited());
    }

    @Test
    public void copyConstructor_preservesValuesWithoutSharingMutableDescriptor() {
        EditClassDescriptor original = populated();
        EditClassDescriptor copy = new EditClassDescriptor(original);
        assertEquals(original, copy);
        original.setModuleCode(new ModuleCode("CS2101"));
        original.setClassName(new ClassName("OB2"));
        assertEquals(new ModuleCode("CS2103"), copy.getModuleCode().orElseThrow());
        assertEquals(new ClassName("OB1"), copy.getClassName().orElseThrow());
    }

    @Test
    public void copyConstructor_emptyDescriptor_preservesAbsentEdits() {
        EditClassDescriptor copy = new EditClassDescriptor(new EditClassDescriptor());
        assertFalse(copy.isAnyFieldEdited());
    }

    @Test
    public void equals_comparesEachField() {
        EditClassDescriptor descriptor = populated();
        assertEquals(descriptor, descriptor);
        assertEquals(populated(), descriptor);
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals("descriptor"));
        EditClassDescriptor changedName = populated();
        changedName.setClassName(new ClassName("OB2"));
        assertFalse(descriptor.equals(changedName));
        EditClassDescriptor changedModule = populated();
        changedModule.setModuleCode(new ModuleCode("CS2101"));
        assertFalse(descriptor.equals(changedModule));
    }

    @Test
    public void toString_containsBothEdits() {
        String expected = EditClassDescriptor.class.getCanonicalName() + "{className=OB1, moduleCode=CS2103}";
        assertEquals(expected, populated().toString());
    }

    private EditClassDescriptor populated() {
        EditClassDescriptor descriptor = new EditClassDescriptor();
        descriptor.setClassName(new ClassName("OB1"));
        descriptor.setModuleCode(new ModuleCode("CS2103"));
        return descriptor;
    }
}
