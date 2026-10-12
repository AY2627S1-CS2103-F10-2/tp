package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class UniquePersonListTest {

    private final UniquePersonList uniquePersonList = new UniquePersonList();

    @Test
    public void contains_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.contains(null));
    }

    @Test
    public void contains_personNotInList_returnsFalse() {
        assertFalse(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personWithSameIdentityFieldsInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB)
                .withTelegram(VALID_TELEGRAM_BOB).build();
        assertTrue(uniquePersonList.contains(editedAlice));
    }

    @Test
    public void contains_personWithSameNameDifferentStudentNumber_returnsFalse() {
        uniquePersonList.add(ALICE);
        Person sameNameAsAlice = new PersonBuilder(BOB).withName(ALICE.getName().fullName).build();
        assertFalse(uniquePersonList.contains(sameNameAsAlice));
    }

    @Test
    public void containsEmail_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsEmail(null));
    }

    @Test
    public void containsEmail() {
        uniquePersonList.add(ALICE);

        // same email, ignoring case -> returns true
        Person sameEmail = new PersonBuilder(BOB).withEmail(ALICE.getEmail().value.toUpperCase()).build();
        assertTrue(uniquePersonList.containsEmail(sameEmail));

        // different email -> returns false
        assertFalse(uniquePersonList.containsEmail(BOB));
    }

    @Test
    public void containsTelegram_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsTelegram(null));
    }

    @Test
    public void containsTelegram() {
        uniquePersonList.add(ALICE);

        // same telegram, ignoring case -> returns true
        Person sameTelegram = new PersonBuilder(BOB).withTelegram("ALICE_pauline").build();
        assertTrue(uniquePersonList.containsTelegram(sameTelegram));

        // different telegram -> returns false
        assertFalse(uniquePersonList.containsTelegram(BOB));

        // person without telegram -> returns false
        assertFalse(uniquePersonList.containsTelegram(CARL));
    }

    @Test
    public void containsTelegram_bothWithoutTelegram_returnsFalse() {
        uniquePersonList.add(CARL);
        Person otherWithoutTelegram = new PersonBuilder(BOB).withTelegram(null).build();
        assertFalse(uniquePersonList.containsTelegram(otherWithoutTelegram));
    }

    @Test
    public void add_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.add(null));
    }

    @Test
    public void add_duplicatePerson_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(ALICE));
    }

    @Test
    public void add_duplicateStudentNumber_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        Person sameStudentNumber = new PersonBuilder(BOB)
                .withStudentNumber(ALICE.getStudentNumber().value.toLowerCase()).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(sameStudentNumber));
    }

    @Test
    public void add_duplicateEmail_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        Person sameEmail = new PersonBuilder(BOB).withEmail(ALICE.getEmail().value.toUpperCase()).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(sameEmail));
    }

    @Test
    public void add_duplicateTelegram_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        Person sameTelegram = new PersonBuilder(BOB).withTelegram("@Alice_Pauline").build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(sameTelegram));
    }

    @Test
    public void add_sameNameDifferentStudentNumber_success() {
        uniquePersonList.add(ALICE);
        Person sameName = new PersonBuilder(BOB).withName(ALICE.getName().fullName).build();
        uniquePersonList.add(sameName);
        assertTrue(uniquePersonList.contains(sameName));
    }

    @Test
    public void add_multiplePersonsWithoutTelegram_success() {
        uniquePersonList.add(CARL);
        Person otherWithoutTelegram = new PersonBuilder(BOB).withTelegram(null).build();
        uniquePersonList.add(otherWithoutTelegram);
        assertTrue(uniquePersonList.contains(otherWithoutTelegram));
    }

    @Test
    public void setPerson_nullTargetPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(null, ALICE));
    }

    @Test
    public void setPerson_nullEditedPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(ALICE, null));
    }

    @Test
    public void setPerson_targetPersonNotInList_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPerson(ALICE, ALICE));
    }

    @Test
    public void setPerson_editedPersonIsSamePerson_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(ALICE);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasSameIdentity_success() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB)
                .withTelegram(VALID_TELEGRAM_BOB).build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(editedAlice);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasDifferentIdentity_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, BOB);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueIdentity_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, BOB));
    }

    @Test
    public void setPerson_editedPersonHasDuplicateEmail_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedAlice = new PersonBuilder(ALICE).withEmail(BOB.getEmail().value).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, editedAlice));
    }

    @Test
    public void setPerson_editedPersonHasDuplicateTelegram_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedAlice = new PersonBuilder(ALICE).withTelegram(VALID_TELEGRAM_BOB).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, editedAlice));
    }

    @Test
    public void setPerson_editedPersonKeepsOwnEmailAndTelegram_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        assertTrue(uniquePersonList.contains(editedAlice));
    }

    @Test
    public void remove_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.remove(null));
    }

    @Test
    public void remove_personDoesNotExist_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.remove(ALICE));
    }

    @Test
    public void remove_existingPerson_removesPerson() {
        uniquePersonList.add(ALICE);
        uniquePersonList.remove(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullUniquePersonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((UniquePersonList) null));
    }

    @Test
    public void setPersons_uniquePersonList_replacesOwnListWithProvidedUniquePersonList() {
        uniquePersonList.add(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        uniquePersonList.setPersons(expectedUniquePersonList);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((List<Person>) null));
    }

    @Test
    public void setPersons_list_replacesOwnListWithProvidedList() {
        uniquePersonList.add(ALICE);
        List<Person> personList = List.of(BOB);
        uniquePersonList.setPersons(personList);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_listWithDuplicatePersons_throwsDuplicatePersonException() {
        List<Person> listWithDuplicatePersons = List.of(ALICE, ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersons));
    }

    @Test
    public void setPersons_listWithDuplicateEmail_throwsDuplicatePersonException() {
        Person sameEmail = new PersonBuilder(BOB).withEmail(ALICE.getEmail().value).build();
        List<Person> list = List.of(ALICE, sameEmail);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(list));
    }

    @Test
    public void setPersons_listWithDuplicateTelegram_throwsDuplicatePersonException() {
        Person sameTelegram = new PersonBuilder(BOB).withTelegram("alice_pauline").build();
        List<Person> list = List.of(ALICE, sameTelegram);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(list));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
            -> uniquePersonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePersonList.asUnmodifiableObservableList().toString(), uniquePersonList.toString());
    }
}
