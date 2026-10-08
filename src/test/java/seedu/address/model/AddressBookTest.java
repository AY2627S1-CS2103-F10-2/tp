package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.academicclass.exceptions.AcademicClassNotFoundException;
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
    public void hasAcademicClass_nullAcademicClass_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasAcademicClass(null));
    }

    @Test
    public void hasAcademicClass_academicClassNotInAddressBook_returnsFalse() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        assertFalse(addressBook.hasAcademicClass(academicClass));
    }

    @Test
    public void hasAcademicClass_academicClassInAddressBook_returnsTrue() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        addressBook.addAcademicClass(academicClass);
        assertTrue(addressBook.hasAcademicClass(academicClass));
    }

    @Test
    public void hasAcademicClass_academicClassWithSameIdentityInAddressBook_returnsTrue() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        AcademicClass sameAcademicClass = new AcademicClass(new ModuleCode("cs2103t"), new ClassName("f10-2"));
        addressBook.addAcademicClass(academicClass);
        assertTrue(addressBook.hasAcademicClass(sameAcademicClass));
    }

    @Test
    public void addAcademicClass_duplicateAcademicClass_throwsDuplicateAcademicClassException() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        addressBook.addAcademicClass(academicClass);
        assertThrows(DuplicateAcademicClassException.class, () -> addressBook.addAcademicClass(academicClass));
    }

    @Test
    public void removeAcademicClass_nullClass_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.removeAcademicClass(null));
    }

    @Test
    public void removeAcademicClass_missingClass_throwsAcademicClassNotFoundException() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
        assertThrows(AcademicClassNotFoundException.class, () -> addressBook.removeAcademicClass(academicClass));
    }

    @Test
    public void removeAcademicClass_existingClass_preservesOtherClassesAndPersons() {
        AcademicClass first = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
        AcademicClass second = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F11-2"));
        addressBook.addAcademicClass(first);
        addressBook.addAcademicClass(second);
        addressBook.addPerson(ALICE);

        addressBook.removeAcademicClass(first);

        assertEquals(List.of(second), addressBook.getAcademicClassList());
        assertEquals(List.of(ALICE), addressBook.getPersonList());
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
        addressBook.addPerson(ALICE);
        addressBook.setAcademicClasses(List.of(academicClass));
        AddressBookStub newData = new AddressBookStub(List.of(BOB), List.of(academicClass, academicClass));
        assertThrows(DuplicateAcademicClassException.class, () -> addressBook.resetData(newData));
        assertEquals(List.of(ALICE), addressBook.getPersonList());
        assertEquals(List.of(academicClass), addressBook.getAcademicClassList());
    }

    @Test
    public void resetData_withNullClass_throwsAndPreservesData() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        addressBook.addPerson(ALICE);
        addressBook.setAcademicClasses(List.of(academicClass));
        AddressBookStub newData = new AddressBookStub(List.of(BOB), Arrays.asList((AcademicClass) null));
        assertThrows(NullPointerException.class, () -> addressBook.resetData(newData));
        assertEquals(List.of(ALICE), addressBook.getPersonList());
        assertEquals(List.of(academicClass), addressBook.getAcademicClassList());
    }

    @Test
    public void resetData_validData_updatesExistingViewsAndSupportsSelfReset() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        ObservableList<Person> personView = addressBook.getPersonList();
        ObservableList<AcademicClass> classView = addressBook.getAcademicClassList();
        addressBook.resetData(new AddressBookStub(List.of(ALICE), List.of(academicClass)));
        assertEquals(List.of(ALICE), personView);
        assertEquals(List.of(academicClass), classView);
        addressBook.resetData(addressBook);
        assertEquals(List.of(ALICE), personView);
        assertEquals(List.of(academicClass), classView);
    }

    @Test
    public void getAcademicClassList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getAcademicClassList().remove(0));
    }

    @Test
    public void equalsAndHashCode_sameData_returnsTrueAndSameHashCode() {
        addressBook.addPerson(ALICE);
        addressBook.setAcademicClasses(List.of(
                new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"))));
        AddressBook copy = new AddressBook(addressBook);
        assertEquals(addressBook, copy);
        assertEquals(addressBook.hashCode(), copy.hashCode());
    }

    @Test
    public void equals() {
        // same object -> returns true
        assertTrue(addressBook.equals(addressBook));

        // null or different type -> returns false
        assertFalse(addressBook.equals(null));
        assertFalse(addressBook.equals(5));

        // same values -> returns true
        assertTrue(addressBook.equals(new AddressBook()));

        // different persons, same classes -> returns false
        AddressBook differentPersons = new AddressBook();
        differentPersons.addPerson(ALICE);
        assertFalse(addressBook.equals(differentPersons));

        // same persons, different classes -> returns false
        AddressBook differentClasses = new AddressBook();
        differentClasses.setAcademicClasses(List.of(
                new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"))));
        assertFalse(addressBook.equals(differentClasses));
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
