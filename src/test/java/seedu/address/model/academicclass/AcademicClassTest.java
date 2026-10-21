package seedu.address.model.academicclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class AcademicClassTest {

    private static final ModuleCode CS2103T = new ModuleCode("CS2103T");
    private static final ModuleCode ST2334 = new ModuleCode("ST2334");
    private static final ClassName F10_2 = new ClassName("F10-2");
    private static final ClassName T24 = new ClassName("T24");

    @Test
    public void constructor_copyMemberships_preservesReferencesInIndependentObservableSet() {
        AcademicClass original = new AcademicClass(CS2103T, F10_2);
        original.assignStudent(ALICE);
        AcademicClass copy = new AcademicClass(ST2334, T24, original);

        assertEquals(ST2334, copy.getModuleCode());
        assertEquals(T24, copy.getClassName());
        assertEquals(Set.of(ALICE), copy.getStudents());
        assertSame(ALICE, copy.getStudents().iterator().next());
        assertNotSame(original.getStudents(), copy.getStudents());
        copy.unassignStudent(ALICE);
        assertEquals(Set.of(ALICE), original.getStudents());
        original.assignStudent(BENSON);
        assertTrue(copy.getStudents().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> copy.getStudents().add(BENSON));
    }

    @Test
    public void constructor_copyEmptyMemberships_createsEmptyClass() {
        AcademicClass copy = new AcademicClass(ST2334, T24, new AcademicClass(CS2103T, F10_2));
        assertTrue(copy.getStudents().isEmpty());
    }

    @Test
    public void constructor_totalLessons_preservesConfiguredValue() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2, 11);
        AcademicClass copy = new AcademicClass(ST2334, T24, academicClass);

        assertEquals(11, academicClass.getTotalLessons());
        assertEquals(11, copy.getTotalLessons());
    }

    @Test
    public void constructor_negativeTotalLessons_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, ()
                -> new AcademicClass(CS2103T, F10_2, -1));
    }

    @Test
    public void constructor_copyWithNullArguments_throwsNullPointerException() {
        AcademicClass original = new AcademicClass(CS2103T, F10_2);
        assertThrows(NullPointerException.class, () -> new AcademicClass(null, T24, original));
        assertThrows(NullPointerException.class, () -> new AcademicClass(ST2334, null, original));
        assertThrows(NullPointerException.class, () -> new AcademicClass(ST2334, T24, null));
    }

    @Test
    public void isSameClass_comparesCombinedIdentity() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);
        assertTrue(academicClass.isSameClass(academicClass));
        assertTrue(academicClass.isSameClass(new AcademicClass(new ModuleCode("cs2103t"), new ClassName("f10-2"))));
        assertFalse(academicClass.isSameClass(null));
        assertFalse(academicClass.isSameClass(new AcademicClass(ST2334, F10_2)));
        assertFalse(academicClass.isSameClass(new AcademicClass(CS2103T, T24)));
    }

    @Test
    public void assignStudent_null_throwsNullPointerException() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);
        assertThrows(NullPointerException.class, () -> academicClass.assignStudent(null));
        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void assignStudent_repeatedAssignment_keepsOneMembership() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);
        assertTrue(academicClass.assignStudent(ALICE));
        assertFalse(academicClass.assignStudent(ALICE));
        assertEquals(Set.of(ALICE), academicClass.getStudents());
        assertTrue(academicClass.assignStudent(BENSON));
        assertEquals(Set.of(ALICE, BENSON), academicClass.getStudents());
        assertThrows(UnsupportedOperationException.class, () -> academicClass.getStudents().add(ALICE));
    }

    @Test
    public void assignStudent_afterUnassign_addsMembershipAgain() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);
        academicClass.assignStudent(ALICE);
        assertTrue(academicClass.unassignStudent(ALICE));
        assertTrue(academicClass.assignStudent(ALICE));
        assertEquals(Set.of(ALICE), academicClass.getStudents());
    }

    @Test
    public void getStudents_modifySet_throwsUnsupportedOperationException() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);
        assertThrows(UnsupportedOperationException.class, () -> academicClass.getStudents().clear());
    }

    @Test
    public void unassignStudent_studentNotAssigned_returnsFalse() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);

        assertFalse(academicClass.unassignStudent(ALICE));
        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void equals() {
        AcademicClass cs2103F10 = new AcademicClass(CS2103T, F10_2);

        // same values -> returns true
        AcademicClass cs2103F10Copy = new AcademicClass(CS2103T, F10_2);
        assertTrue(cs2103F10.equals(cs2103F10Copy));

        // same normalized values -> returns true
        AcademicClass cs2103F10NormalizedCopy = new AcademicClass(new ModuleCode("cs2103t"), new ClassName("f10-2"));
        assertTrue(cs2103F10.equals(cs2103F10NormalizedCopy));

        // same object -> returns true
        assertTrue(cs2103F10.equals(cs2103F10));

        // null -> returns false
        assertFalse(cs2103F10.equals(null));

        // different types -> returns false
        assertFalse(cs2103F10.equals(5));

        // different module code -> returns false
        AcademicClass differentModuleCode = new AcademicClass(ST2334, F10_2);
        assertFalse(cs2103F10.equals(differentModuleCode));

        // different class name -> returns false
        AcademicClass differentClassName = new AcademicClass(CS2103T, T24);
        assertFalse(cs2103F10.equals(differentClassName));
    }

    @Test
    public void toStringMethod() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);
        String expected = academicClass.getModuleCode() + "-" + academicClass.getClassName();
        assertEquals(expected, academicClass.toString());
    }

    @Test
    public void hasIdentity_sameModuleCodeAndClassName_returnsTrue() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);

        assertTrue(academicClass.hasIdentity(CS2103T, F10_2));
    }

    @Test
    public void hasIdentity_differentClassName_returnsFalse() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);

        assertFalse(academicClass.hasIdentity(CS2103T, T24));
    }

    @Test
    public void hasIdentity_differentModuleCode_returnsFalse() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);

        assertFalse(academicClass.hasIdentity(ST2334, F10_2));
    }
}
