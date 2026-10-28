package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.academicclass.exceptions.AcademicClassNotFoundException;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Tests the lifecycle of the active class-based person filter.
 */
public class ModelManagerClassFilterTest {
    private ModelManager model;
    private AcademicClass first;
    private AcademicClass second;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        // Reverse insertion order makes alphabetical sorting observable.
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        first = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F01"));
        second = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F02"));
        model.addAcademicClass(first);
        model.addAcademicClass(second);
        model.assignStudentToClass(ALICE, first);
    }

    @Test
    public void filterPersonsByClass_membershipChanges_refreshAlphabetically() {
        model.filterPersonsByClass(first);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertTrue(model.assignStudentToClass(BENSON, first));
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertFalse(model.assignStudentToClass(BENSON, first));
        assertTrue(model.unassignStudentFromClass(ALICE, first));
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertFalse(model.unassignStudentFromClass(ALICE, first));
    }

    @Test
    public void filterPersonsByClass_switchClass_usesNewMemberships() {
        model.filterPersonsByClass(first);
        model.filterPersonsByClass(second);
        assertTrue(model.getFilteredPersonList().isEmpty());
        model.assignStudentToClass(BENSON, second);
        model.unassignStudentFromClass(ALICE, first);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void filterPersonsByClass_null_preservesExistingFilter() {
        model.filterPersonsByClass(first);
        assertThrows(NullPointerException.class, () -> model.filterPersonsByClass(null));
        model.assignStudentToClass(BENSON, first);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_nameFilter_clearsClassTracking() {
        model.filterPersonsByClass(first);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        model.deleteAcademicClass(first);
        model.assignStudentToClass(ALICE, second);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_showAll_restoresInsertionOrder() {
        model.filterPersonsByClass(first);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        model.unassignStudentFromClass(ALICE, first);
        assertEquals(List.of(BENSON, ALICE), model.getFilteredPersonList());
    }

    @Test
    public void deleteAcademicClass_viewedClass_clearsViewAndFollowsRecreatedClass() {
        model.filterPersonsByClass(first);
        AcademicClass equivalent = new AcademicClass(first.getModuleCode(), first.getClassName());
        model.deleteAcademicClass(equivalent);
        assertTrue(model.getFilteredPersonList().isEmpty());
        AcademicClass recreated = new AcademicClass(first.getModuleCode(), first.getClassName());
        model.addAcademicClass(recreated);
        model.assignStudentToClass(BENSON, recreated);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void setAcademicClass_renamedToDeletedViewedClass_refreshesStudents() {
        model.assignStudentToClass(BENSON, second);
        model.filterPersonsByClassAndDisplayAttendance(first, Optional.empty());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        model.deleteAcademicClass(first);
        assertTrue(model.getFilteredPersonList().isEmpty());

        AcademicClass renamed = new AcademicClass(first.getModuleCode(), first.getClassName(), second);
        model.setAcademicClass(second, renamed);

        assertTrue(model.hasAcademicClass(renamed));
        assertTrue(renamed.getStudents().contains(BENSON));
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertSame(renamed, model.getAttendanceDisplayClass().orElseThrow());
    }

    @Test
    public void setAcademicClass_renamedToDeletedViewedClass_preservesThresholdAndOrder() {
        model.assignStudentToClass(BENSON, second);
        model.assignStudentToClass(ALICE, second);
        LocalDate firstDate = LocalDate.of(2024, 1, 1);
        LocalDate secondDate = LocalDate.of(2024, 1, 2);
        model.addAttendance(ALICE, second, firstDate);
        model.addAttendance(BENSON, second, firstDate);
        model.addAttendance(BENSON, second, secondDate);
        model.filterPersonsByClassAndDisplayAttendance(first, Optional.of(2));
        model.deleteAcademicClass(first);

        AcademicClass renamed = new AcademicClass(first.getModuleCode(), first.getClassName(), second);
        model.setAcademicClass(second, renamed);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertSame(renamed, model.getAttendanceDisplayClass().orElseThrow());
        model.removeAttendance(BENSON, renamed, secondDate);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void setAcademicClass_attendanceDisplayCleared_keepsDisplayCleared() {
        model.assignStudentToClass(BENSON, second);
        model.filterPersonsByClassAndDisplayAttendance(first, Optional.empty());
        model.deleteAcademicClass(first);
        model.clearAttendanceDisplayClass();

        AcademicClass renamed = new AcademicClass(first.getModuleCode(), first.getClassName(), second);
        model.setAcademicClass(second, renamed);

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertTrue(model.getAttendanceDisplayClass().isEmpty());
        AcademicClass renamedAgain = new AcademicClass(first.getModuleCode(), new ClassName("F03"), renamed);
        model.setAcademicClass(renamed, renamedAgain);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertTrue(model.getAttendanceDisplayClass().isEmpty());
    }

    @Test
    public void setAcademicClass_otherClass_preservesActiveView() {
        model.assignStudentToClass(BENSON, second);
        model.filterPersonsByClassAndDisplayAttendance(first, Optional.empty());
        AcademicClass renamed = new AcademicClass(second.getModuleCode(), new ClassName("F03"), second);

        model.setAcademicClass(second, renamed);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertSame(first, model.getAttendanceDisplayClass().orElseThrow());
    }

    @Test
    public void setAcademicClass_afterFind_preservesSearch() {
        model.assignStudentToClass(BENSON, second);
        model.filterPersonsByClassAndDisplayAttendance(first, Optional.empty());
        model.deleteAcademicClass(first);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        AcademicClass renamed = new AcademicClass(first.getModuleCode(), first.getClassName(), second);

        model.setAcademicClass(second, renamed);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertTrue(model.getAttendanceDisplayClass().isEmpty());
    }

    @Test
    public void setAcademicClass_afterList_preservesAllStudentsInInsertionOrder() {
        model.assignStudentToClass(BENSON, second);
        model.filterPersonsByClassAndDisplayAttendance(first, Optional.empty());
        model.deleteAcademicClass(first);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        AcademicClass renamed = new AcademicClass(first.getModuleCode(), first.getClassName(), second);

        model.setAcademicClass(second, renamed);

        assertEquals(List.of(BENSON, ALICE), model.getFilteredPersonList());
        assertTrue(model.getAttendanceDisplayClass().isEmpty());
    }

    @Test
    public void deleteAcademicClass_otherClass_preservesClassFilter() {
        model.filterPersonsByClass(first);
        model.deleteAcademicClass(second);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        model.assignStudentToClass(BENSON, first);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void deleteAcademicClass_missingClass_preservesClassFilter() {
        model.filterPersonsByClass(first);
        AcademicClass missing = new AcademicClass(new ModuleCode("CS2101"), new ClassName("F03"));
        assertThrows(AcademicClassNotFoundException.class, () -> model.deleteAcademicClass(missing));
        model.assignStudentToClass(BENSON, first);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void setAddressBook_replacement_discardsOldClassFilter() {
        model.filterPersonsByClass(first);
        AddressBook replacement = new AddressBook();
        replacement.addPerson(BENSON);
        model.setAddressBook(replacement);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        model.setAddressBook(new AddressBook());
        assertTrue(model.getFilteredPersonList().isEmpty());
        model.addPerson(ALICE);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void setAddressBook_null_preservesClassFilter() {
        model.filterPersonsByClass(first);
        assertThrows(NullPointerException.class, () -> model.setAddressBook(null));
        model.assignStudentToClass(BENSON, first);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }
}
