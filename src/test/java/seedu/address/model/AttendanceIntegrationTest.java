package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

/** Tests attendance ownership and lifecycle consistency without command parsing or storage. */
public class AttendanceIntegrationTest {
    private static final LocalDate DATE = LocalDate.of(2000, 1, 1);
    private final AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
    private final ModelManager model = new ModelManager();

    @BeforeEach
    public void setUp() {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addAcademicClass(academicClass);
        model.assignStudentToClass(ALICE, academicClass);
    }

    @Test
    public void addAttendance_samePair_keepsOneAssociation() {
        assertTrue(model.addAttendance(ALICE, academicClass, DATE));
        assertFalse(model.addAttendance(ALICE, academicClass, DATE));
        assertTrue(model.addAttendance(ALICE, academicClass, DATE.plusDays(1)));
        assertEquals(1, model.getAttendanceList().size());
        assertEquals(Set.of(DATE, DATE.plusDays(1)), model.getAttendanceList().get(0).getAttendedDates());
    }

    @Test
    public void changeAttendance_unknownEndpoints_preservesState() {
        AcademicClass missingClass = new AcademicClass(new ModuleCode("CS2101"), new ClassName("T01"));
        Person missingPerson = new PersonBuilder(ALICE).withStudentNumber("A9999999X").build();
        assertThrows(IllegalArgumentException.class, () -> model.addAttendance(ALICE, missingClass, DATE));
        assertThrows(IllegalArgumentException.class, () -> model.addAttendance(missingPerson, academicClass, DATE));
        assertThrows(IllegalArgumentException.class, () -> model.removeAttendance(ALICE, missingClass, DATE));
        assertEquals(List.of(), model.getAttendanceList());
    }

    @Test
    public void changeAttendance_invalidDate_doesNotCreateAssociation() {
        assertThrows(IllegalArgumentException.class, () -> model.addAttendance(ALICE, academicClass, LocalDate.MAX));
        assertThrows(NullPointerException.class, () -> model.addAttendance(ALICE, academicClass, null));
        assertThrows(NullPointerException.class, () -> model.removeAttendance(ALICE, academicClass, null));
        assertFalse(model.removeAttendance(ALICE, academicClass, DATE));
        assertEquals(List.of(), model.getAttendanceList());
    }

    @Test
    public void getAttendanceList_mutation_doesNotChangeOwnedDates() {
        model.addAttendance(ALICE, academicClass, DATE);
        var snapshot = model.getAttendanceList();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        snapshot.get(0).removeDate(DATE);
        assertEquals(Set.of(DATE), model.getAttendanceList().get(0).getAttendedDates());
    }

    @Test
    public void unassignReassign_preservesHistoryAndAllowsCorrection() {
        model.addAttendance(ALICE, academicClass, DATE);
        model.addAttendance(ALICE, academicClass, DATE.plusDays(1));
        model.unassignStudentFromClass(ALICE, academicClass);
        assertEquals(2, model.getAttendanceList().get(0).getAttendanceCount());
        assertTrue(model.removeAttendance(ALICE, academicClass, DATE));
        model.assignStudentToClass(ALICE, academicClass);
        assertEquals(Set.of(DATE.plusDays(1)), model.getAttendanceList().get(0).getAttendedDates());
        assertEquals(1, model.getAttendanceList().size());
    }

    @Test
    public void setPerson_unassignedStudentNumberChanges_preservesHistory() {
        model.addAttendance(ALICE, academicClass, DATE);
        model.unassignStudentFromClass(ALICE, academicClass);
        Person edited = new PersonBuilder(ALICE).withStudentNumber("A9999999X").withName("Updated Alice").build();
        model.setPerson(ALICE, edited);
        assertSame(edited, model.getAttendanceList().get(0).getStudent());
        assertEquals(Set.of(DATE), model.getAttendanceList().get(0).getAttendedDates());
        assertTrue(model.removeAttendance(edited, academicClass, DATE));
    }

    @Test
    public void setPerson_otherStudentsAttendance_preservesHistory() {
        model.addAttendance(ALICE, academicClass, DATE);
        model.addAttendance(BENSON, academicClass, DATE.plusDays(1));
        Person edited = new PersonBuilder(ALICE).withName("Updated Alice").build();

        model.setPerson(ALICE, edited);

        assertEquals(2, model.getAttendanceList().size());
        assertSame(edited, model.getAttendanceList().get(0).getStudent());
        assertSame(BENSON, model.getAttendanceList().get(1).getStudent());
        assertEquals(Set.of(DATE.plusDays(1)), model.getAttendanceList().get(1).getAttendedDates());
    }

    @Test
    public void setPerson_duplicateIdentity_preservesHistory() {
        model.addAttendance(ALICE, academicClass, DATE);
        assertThrows(DuplicatePersonException.class, () -> model.setPerson(ALICE, BENSON));
        assertSame(ALICE, model.getAttendanceList().get(0).getStudent());
        assertEquals(Set.of(DATE), model.getAttendanceList().get(0).getAttendedDates());
    }

    @Test
    public void deletePerson_removesOnlyTheirAttendance() {
        model.addAttendance(ALICE, academicClass, DATE);
        model.addAttendance(BENSON, academicClass, DATE);
        model.deletePerson(ALICE);
        assertEquals(1, model.getAttendanceList().size());
        assertSame(BENSON, model.getAttendanceList().get(0).getStudent());
    }

    @Test
    public void deleteClass_removesOnlyItsAttendance() {
        AcademicClass otherClass = new AcademicClass(new ModuleCode("CS2101"), new ClassName("T01"));
        model.addAcademicClass(otherClass);
        model.addAttendance(ALICE, academicClass, DATE);
        model.addAttendance(ALICE, otherClass, DATE);
        model.deleteAcademicClass(academicClass);
        assertEquals(1, model.getAttendanceList().size());
        assertSame(otherClass, model.getAttendanceList().get(0).getAcademicClass());
    }

    @Test
    public void copyAndReset_preservesIndependentAttendance() {
        model.addAttendance(ALICE, academicClass, DATE);
        ModelManager copy = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertEquals(model, copy);
        copy.removeAttendance(ALICE, academicClass, DATE);
        assertEquals(Set.of(DATE), model.getAttendanceList().get(0).getAttendedDates());
        assertFalse(model.equals(copy));
        model.setAddressBook(new AddressBook());
        assertEquals(List.of(), model.getAttendanceList());
    }

    @Test
    public void bulkReplacement_rebindsRetainedEndpointsAndDropsRemovedOnes() {
        AddressBook book = new AddressBook(model.getAddressBook());
        book.addAttendance(ALICE, academicClass, DATE);
        Person edited = new PersonBuilder(ALICE).withName("Updated Alice").build();
        book.setPersons(List.of(edited));
        AcademicClass replacement = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
        book.setAcademicClasses(List.of(replacement));
        assertSame(edited, book.getAttendanceList().get(0).getStudent());
        assertSame(replacement, book.getAttendanceList().get(0).getAcademicClass());
        book.setPersons(List.of());
        assertEquals(List.of(), book.getAttendanceList());
    }

    @Test
    public void addAttendance_equivalentEndpoints_usesStoredObjects() {
        Person equivalent = new PersonBuilder(ALICE).withName("Outdated Name").build();
        AcademicClass equivalentClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
        model.addAttendance(equivalent, equivalentClass, DATE);
        model.addAttendance(ALICE, academicClass, DATE.plusDays(1));
        assertEquals(1, model.getAttendanceList().size());
        assertSame(ALICE, model.getAttendanceList().get(0).getStudent());
        assertSame(academicClass, model.getAttendanceList().get(0).getAcademicClass());
    }

    @Test
    public void resetData_orphanedAttendance_preservesExistingData() {
        model.addAttendance(ALICE, academicClass, DATE);
        AddressBook before = new AddressBook(model.getAddressBook());
        AddressBook invalid = new AddressBook() {
            @Override
            public List<Attendance> getAttendanceList() {
                return List.of(new Attendance(ALICE, academicClass));
            }
        };
        assertThrows(IllegalArgumentException.class, () -> model.setAddressBook(invalid));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void resetData_duplicateAttendance_preservesExistingData() {
        model.addAttendance(ALICE, academicClass, DATE);
        AddressBook before = new AddressBook(model.getAddressBook());
        AddressBook invalid = new AddressBook(model.getAddressBook()) {
            @Override
            public List<Attendance> getAttendanceList() {
                Attendance record = new Attendance(ALICE, academicClass);
                return List.of(record, record);
            }
        };
        assertThrows(IllegalArgumentException.class, () -> model.setAddressBook(invalid));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void setAcademicClasses_removedClass_clearsItsHistory() {
        AddressBook book = new AddressBook(model.getAddressBook());
        book.addAttendance(ALICE, academicClass, DATE);
        book.setAcademicClasses(List.of());
        assertEquals(List.of(), book.getAttendanceList());
    }

    @Test
    public void setAttendance_duplicateOrOrphanedRecords_preservesHistory() {
        AddressBook book = new AddressBook(model.getAddressBook());
        book.addAttendance(ALICE, academicClass, DATE);
        List<Attendance> original = book.getAttendanceList();
        Attendance record = original.get(0);
        assertThrows(IllegalArgumentException.class, () -> book.setAttendance(List.of(record, record)));
        Person missing = new PersonBuilder(ALICE).withStudentNumber("A9999999X").build();
        assertThrows(IllegalArgumentException.class, () -> book.setAttendance(
                List.of(record.copyFor(missing, academicClass))));
        assertEquals(original, book.getAttendanceList());
    }

    @Test
    public void setAttendance_missingClass_preservesHistory() {
        AddressBook book = new AddressBook(model.getAddressBook());
        book.addAttendance(ALICE, academicClass, DATE);
        List<Attendance> original = book.getAttendanceList();
        AcademicClass missingClass = new AcademicClass(new ModuleCode("CS2101"), new ClassName("T01"));
        Attendance invalid = original.get(0).copyFor(ALICE, missingClass);

        assertThrows(IllegalArgumentException.class, () -> book.setAttendance(List.of(original.get(0), invalid)));
        assertEquals(original, book.getAttendanceList());
    }

    @Test
    public void setAttendance_distinctPairs_preservesSeparateHistories() {
        AddressBook book = new AddressBook(model.getAddressBook());
        AcademicClass otherClass = new AcademicClass(new ModuleCode("CS2101"), new ClassName("T01"));
        book.addAcademicClass(otherClass);
        Attendance first = new Attendance(ALICE, academicClass);
        Attendance otherMembership = new Attendance(ALICE, otherClass);
        Attendance otherStudent = new Attendance(BENSON, academicClass);
        first.addDate(DATE);
        otherMembership.addDate(DATE.plusDays(1));
        otherStudent.addDate(DATE.plusDays(2));

        book.setAttendance(List.of(first, otherMembership, otherStudent));

        assertEquals(List.of(first, otherMembership, otherStudent), book.getAttendanceList());
        assertEquals(book, new AddressBook(book));
    }

    @Test
    public void setAttendance_validRecords_copiesDatesIndependently() {
        AddressBook book = new AddressBook(model.getAddressBook());
        Attendance record = new Attendance(ALICE, academicClass);
        record.addDate(DATE);
        book.setAttendance(List.of(record));
        record.removeDate(DATE);
        assertEquals(Set.of(DATE), book.getAttendanceList().get(0).getAttendedDates());
        book.setAttendance(List.of());
        assertEquals(List.of(), book.getAttendanceList());
    }
}
