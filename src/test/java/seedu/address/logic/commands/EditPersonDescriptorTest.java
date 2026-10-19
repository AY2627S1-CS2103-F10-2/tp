package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STUDENT_NUMBER_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different student number -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withStudentNumber(VALID_STUDENT_NUMBER_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different telegram -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTelegram(VALID_TELEGRAM_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different tags -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void telegramRemoval_distinctFromUnchangedAndPreservedByCopy() {
        EditPersonDescriptor unchanged = new EditPersonDescriptor();
        EditPersonDescriptor unchangedCopy = new EditPersonDescriptor(unchanged);
        assertEquals(unchanged, unchangedCopy);
        assertFalse(unchangedCopy.isAnyFieldEdited());
        assertFalse(unchangedCopy.isTelegramEdited());

        EditPersonDescriptor removal = new EditPersonDescriptor();
        removal.setTelegram(null);
        assertFalse(unchanged.equals(removal));
        assertTrue(removal.isAnyFieldEdited());
        assertTrue(removal.isTelegramEdited());
        assertTrue(removal.getTelegram().isEmpty());

        EditPersonDescriptor removalCopy = new EditPersonDescriptor(removal);
        assertEquals(removal, removalCopy);
        assertTrue(removalCopy.isTelegramEdited());
        assertTrue(removalCopy.isAnyFieldEdited());
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{studentNumber="
                + editPersonDescriptor.getStudentNumber().orElse(null) + ", name="
                + editPersonDescriptor.getName().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", telegram="
                + editPersonDescriptor.getTelegram().orElse(null) + ", telegramEdited="
                + editPersonDescriptor.isTelegramEdited() + ", tags="
                + editPersonDescriptor.getTags().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
