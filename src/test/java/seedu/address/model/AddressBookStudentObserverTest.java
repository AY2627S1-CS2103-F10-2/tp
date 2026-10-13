package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.academicclass.exceptions.DuplicateAcademicClassException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests student observer registration and notifications through address book operations.
 */
public class AddressBookStudentObserverTest {
    private final AddressBook addressBook = new AddressBook();

    @Test
    public void setPersons_validReplacement_reconcilesMembershipsAcrossAllClasses() {
        AcademicClass first = classWithAlice("OB1");
        AcademicClass second = classWithAlice("OB2");
        AcademicClass unrelated = new AcademicClass(new ModuleCode("CS2103"), new ClassName("OB3"));
        first.assignStudent(BENSON);
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);
        addressBook.addAcademicClass(first);
        addressBook.addAcademicClass(second);
        addressBook.addAcademicClass(unrelated);
        Person updatedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();
        Person newStudent = new PersonBuilder(BENSON).withStudentNumber("A8888883K").build();

        addressBook.setPersons(List.of(updatedAlice, newStudent));

        assertEquals(List.of(updatedAlice, newStudent), addressBook.getPersonList());
        assertEquals(Set.of(updatedAlice), first.getStudents());
        assertSame(updatedAlice, first.getStudents().iterator().next());
        assertSame(updatedAlice, second.getStudents().iterator().next());
        assertTrue(unrelated.getStudents().isEmpty());
    }

    @Test
    public void setPersons_emptyReplacement_clearsAllMemberships() {
        AcademicClass academicClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(academicClass);

        addressBook.setPersons(List.of());

        assertTrue(addressBook.getPersonList().isEmpty());
        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void setPersons_duplicateReplacement_preservesPersonsAndMemberships() {
        AcademicClass academicClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(academicClass);
        Person updatedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        assertThrows(DuplicatePersonException.class, () ->
                addressBook.setPersons(List.of(ALICE, updatedAlice)));

        assertEquals(List.of(ALICE), addressBook.getPersonList());
        assertSame(ALICE, academicClass.getStudents().iterator().next());
    }

    @Test
    public void setPerson_assignedToMultipleClasses_updatesOnlyExistingMemberships() {
        AcademicClass first = classWithAlice("OB1");
        AcademicClass second = classWithAlice("OB2");
        AcademicClass unrelated = new AcademicClass(new ModuleCode("CS2103"), new ClassName("OB3"));
        first.assignStudent(BENSON);
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);
        addressBook.addAcademicClass(first);
        addressBook.addAcademicClass(second);
        addressBook.addAcademicClass(unrelated);
        Person editedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        addressBook.setPerson(ALICE, editedAlice);

        assertEquals(Set.of(editedAlice, BENSON), first.getStudents());
        assertSame(editedAlice, second.getStudents().iterator().next());
        assertTrue(unrelated.getStudents().isEmpty());
        assertSame(editedAlice, addressBook.getPersonList().getFirst());
        assertTrue(addressBook.unassignStudentFromClass(editedAlice, first));
        assertEquals(Set.of(BENSON), first.getStudents());
    }

    @Test
    public void setPerson_studentNumberChanged_updatesMembershipIdentity() {
        AcademicClass academicClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(academicClass);
        Person editedAlice = new PersonBuilder(ALICE).withStudentNumber("A8888883K").build();

        addressBook.setPerson(ALICE, editedAlice);

        assertTrue(addressBook.findPersonByStudentNumber(ALICE.getStudentNumber()).isEmpty());
        assertSame(editedAlice, academicClass.getStudents().iterator().next());
        assertTrue(addressBook.unassignStudentFromClass(editedAlice, academicClass));
    }

    @Test
    public void setPerson_duplicateIdentity_preservesPersonAndMemberships() {
        AcademicClass academicClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);
        addressBook.addAcademicClass(academicClass);
        Person invalidReplacement = new PersonBuilder(ALICE)
                .withStudentNumber(BENSON.getStudentNumber().value).build();

        assertThrows(DuplicatePersonException.class, () -> addressBook.setPerson(ALICE, invalidReplacement));

        assertEquals(List.of(ALICE, BENSON), addressBook.getPersonList());
        assertSame(ALICE, academicClass.getStudents().iterator().next());
    }

    @Test
    public void removePerson_assignedToMultipleClasses_removesAllMemberships() {
        AcademicClass first = classWithAlice("OB1");
        AcademicClass second = classWithAlice("OB2");
        first.assignStudent(BENSON);
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);
        addressBook.addAcademicClass(first);
        addressBook.addAcademicClass(second);

        addressBook.removePerson(ALICE);
        addressBook.addPerson(ALICE);

        assertEquals(Set.of(BENSON), first.getStudents());
        assertTrue(second.getStudents().isEmpty());
        assertEquals(List.of(BENSON, ALICE), addressBook.getPersonList());
    }

    @Test
    public void constructor_copiedClasses_receiveStudentUpdates() {
        AcademicClass academicClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(academicClass);
        AddressBook copied = new AddressBook(addressBook);
        Person editedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        copied.setPerson(ALICE, editedAlice);

        assertSame(editedAlice, copied.getAcademicClassList().getFirst().getStudents().iterator().next());
    }

    @Test
    public void resetData_replacementClasses_receiveStudentDeletion() {
        AddressBook replacement = new AddressBook();
        AcademicClass academicClass = classWithAlice("OB1");
        replacement.addPerson(ALICE);
        replacement.addAcademicClass(academicClass);
        addressBook.resetData(replacement);

        addressBook.removePerson(ALICE);

        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void resetData_selfReset_preservesRegistrations() {
        AcademicClass academicClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(academicClass);
        addressBook.resetData(addressBook);

        addressBook.removePerson(ALICE);

        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void resetData_replacedClasses_noLongerReceiveNotifications() {
        AcademicClass removedClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(removedClass);
        AddressBook replacement = new AddressBook();
        replacement.addPerson(ALICE);
        addressBook.resetData(replacement);

        addressBook.removePerson(ALICE);

        assertEquals(Set.of(ALICE), removedClass.getStudents());
    }

    @Test
    public void setAcademicClasses_replacementClasses_receiveStudentUpdates() {
        AcademicClass replacementClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.setAcademicClasses(List.of(replacementClass));
        Person editedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        addressBook.setPerson(ALICE, editedAlice);

        assertSame(editedAlice, replacementClass.getStudents().iterator().next());
    }

    @Test
    public void setAcademicClasses_removedClasses_noLongerReceiveNotifications() {
        AcademicClass removedClass = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(removedClass);
        addressBook.setAcademicClasses(List.of());

        addressBook.removePerson(ALICE);

        assertEquals(Set.of(ALICE), removedClass.getStudents());
    }

    @Test
    public void setAcademicClasses_invalidReplacement_preservesRegistrations() {
        AcademicClass original = classWithAlice("OB1");
        addressBook.addPerson(ALICE);
        addressBook.addAcademicClass(original);
        AcademicClass duplicate = classWithAlice("OB1");

        assertThrows(DuplicateAcademicClassException.class, () ->
                addressBook.setAcademicClasses(List.of(original, duplicate)));
        addressBook.removePerson(ALICE);

        assertTrue(original.getStudents().isEmpty());
        assertEquals(List.of(original), addressBook.getAcademicClassList());
        assertEquals(Set.of(ALICE), duplicate.getStudents());
    }

    private AcademicClass classWithAlice(String className) {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName(className));
        academicClass.assignStudent(ALICE);
        return academicClass;
    }
}
