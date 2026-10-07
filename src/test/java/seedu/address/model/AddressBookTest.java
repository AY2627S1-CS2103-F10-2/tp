package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.academicclass.exceptions.DuplicateAcademicClassException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
        assertEquals(List.of(), addressBook.getAcademicClassList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons, List.of());

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", academicClasses=" + addressBook.getAcademicClassList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    @Test
    public void constructor_copy_preservesAcademicClasses() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        addressBook.setAcademicClasses(List.of(academicClass));
        AddressBook copy = new AddressBook(addressBook);
        assertEquals(List.of(academicClass), copy.getAcademicClassList());
        addressBook.setAcademicClasses(List.of());
        assertEquals(List.of(academicClass), copy.getAcademicClassList());
    }

    @Test
    public void resetData_withAcademicClasses_replacesAndClearsClasses() {
        AcademicClass first = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        AcademicClass second = new AcademicClass(new ModuleCode("ST2334"), new ClassName("T24"));
        addressBook.setAcademicClasses(List.of(first));
        addressBook.resetData(new AddressBookStub(List.of(), List.of(second)));
        assertEquals(List.of(second), addressBook.getAcademicClassList());
        addressBook.resetData(new AddressBook());
        assertTrue(addressBook.getAcademicClassList().isEmpty());
    }

    @Test
    public void resetData_withDuplicateClasses_throwsDuplicateAcademicClassException() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        AddressBookStub newData = new AddressBookStub(List.of(), List.of(academicClass, academicClass));
        assertThrows(DuplicateAcademicClassException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void getAcademicClassList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getAcademicClassList().remove(0));
    }

    @Test
    public void equalsAndHashCode_sameData_returnsTrueAndSameHashCode() {
        AddressBook copy = new AddressBook(addressBook);
        assertEquals(addressBook, copy);
        assertEquals(addressBook.hashCode(), copy.hashCode());
    }

    /**
     * A stub ReadOnlyAddressBook whose lists can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<AcademicClass> academicClasses = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons, Collection<AcademicClass> academicClasses) {
            this.persons.setAll(persons);
            this.academicClasses.setAll(academicClasses);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<AcademicClass> getAcademicClassList() {
            return academicClasses;
        }
    }

}
