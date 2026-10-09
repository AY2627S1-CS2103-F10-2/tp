package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STUDENT_NUMBER_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void constructor_nullRequiredField_throwsNullPointerException() {
        Set<Tag> tags = new HashSet<>();
        assertThrows(NullPointerException.class, ()
                -> new Person(null, ALICE.getName(), ALICE.getEmail(), null, tags));
        assertThrows(NullPointerException.class, ()
                -> new Person(ALICE.getStudentNumber(), null, ALICE.getEmail(), null, tags));
        assertThrows(NullPointerException.class, ()
                -> new Person(ALICE.getStudentNumber(), ALICE.getName(), null, null, tags));
        assertThrows(NullPointerException.class, ()
                -> new Person(ALICE.getStudentNumber(), ALICE.getName(), ALICE.getEmail(), null, null));
    }

    @Test
    public void constructor_nullTelegram_success() {
        Person person = new Person(ALICE.getStudentNumber(), ALICE.getName(), ALICE.getEmail(), null,
                new HashSet<>());
        assertTrue(person.getTelegram().isEmpty());
    }

    @Test
    public void getTags_modifyTags_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void getTelegram() {
        // person with telegram -> present
        assertEquals(new Telegram("alice_pauline"), ALICE.getTelegram().get());

        // person without telegram -> empty
        assertTrue(CARL.getTelegram().isEmpty());
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same student number, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB)
                .withTelegram(VALID_TELEGRAM_BOB).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different student number, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withStudentNumber(VALID_STUDENT_NUMBER_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // same name, different student number -> returns false
        editedAlice = new PersonBuilder(BOB).withName(ALICE.getName().fullName).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // student number differs in case, all other attributes same -> returns true
        editedAlice = new PersonBuilder(ALICE)
                .withStudentNumber(ALICE.getStudentNumber().value.toLowerCase()).build();
        assertTrue(ALICE.isSamePerson(editedAlice));
    }

    @Test
    public void hasSameEmail() {
        // same object -> returns true
        assertTrue(ALICE.hasSameEmail(ALICE));

        // null -> returns false
        assertFalse(ALICE.hasSameEmail(null));

        // same email, all other attributes different -> returns true
        Person editedBob = new PersonBuilder(BOB).withEmail(ALICE.getEmail().value).build();
        assertTrue(ALICE.hasSameEmail(editedBob));

        // email differs in case -> returns true
        editedBob = new PersonBuilder(BOB).withEmail(ALICE.getEmail().value.toUpperCase()).build();
        assertTrue(ALICE.hasSameEmail(editedBob));

        // different email, all other attributes same -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.hasSameEmail(editedAlice));
    }

    @Test
    public void hasSameTelegram() {
        // same object with telegram -> returns true
        assertTrue(ALICE.hasSameTelegram(ALICE));

        // null -> returns false
        assertFalse(ALICE.hasSameTelegram(null));

        // same telegram, all other attributes different -> returns true
        Person editedBob = new PersonBuilder(BOB).withTelegram("alice_pauline").build();
        assertTrue(ALICE.hasSameTelegram(editedBob));

        // telegram differs in case -> returns true
        editedBob = new PersonBuilder(BOB).withTelegram("ALICE_Pauline").build();
        assertTrue(ALICE.hasSameTelegram(editedBob));

        // telegram differs only by a leading '@' -> returns true
        editedBob = new PersonBuilder(BOB).withTelegram("@alice_pauline").build();
        assertTrue(ALICE.hasSameTelegram(editedBob));

        // different telegram -> returns false
        assertFalse(ALICE.hasSameTelegram(BOB));

        // this person has no telegram, other has one -> returns false
        assertFalse(CARL.hasSameTelegram(ALICE));

        // other person has no telegram -> returns false
        assertFalse(ALICE.hasSameTelegram(CARL));

        // both have no telegram -> returns false
        Person editedCarl = new PersonBuilder(CARL).withStudentNumber(VALID_STUDENT_NUMBER_BOB).build();
        assertFalse(CARL.hasSameTelegram(editedCarl));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));
        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());

        // same values, no telegram -> returns true
        Person carlCopy = new PersonBuilder(CARL).build();
        assertTrue(CARL.equals(carlCopy));
        assertEquals(CARL.hashCode(), carlCopy.hashCode());

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different student number -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withStudentNumber(VALID_STUDENT_NUMBER_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different name -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different telegram -> returns false
        editedAlice = new PersonBuilder(ALICE).withTelegram(VALID_TELEGRAM_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // telegram removed -> returns false
        editedAlice = new PersonBuilder(ALICE).withTelegram(null).build();
        assertFalse(ALICE.equals(editedAlice));

        // telegram added -> returns false
        Person editedCarl = new PersonBuilder(CARL).withTelegram(VALID_TELEGRAM_BOB).build();
        assertFalse(CARL.equals(editedCarl));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{studentNumber=" + ALICE.getStudentNumber()
                + ", name=" + ALICE.getName() + ", email=" + ALICE.getEmail()
                + ", telegram=" + ALICE.getTelegram().get() + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());

        // no telegram
        expected = Person.class.getCanonicalName() + "{studentNumber=" + CARL.getStudentNumber()
                + ", name=" + CARL.getName() + ", email=" + CARL.getEmail()
                + ", telegram=null, tags=" + CARL.getTags() + "}";
        assertEquals(expected, CARL.toString());
    }
}
