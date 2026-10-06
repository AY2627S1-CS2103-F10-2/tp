package seedu.address.model.academicclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AcademicClassTest {

    private static final ModuleCode CS2103T = new ModuleCode("CS2103T");
    private static final ModuleCode ST2334 = new ModuleCode("ST2334");
    private static final ClassName F10_2 = new ClassName("F10-2");
    private static final ClassName T24 = new ClassName("T24");

    @Test
    public void getStudents_modifySet_throwsUnsupportedOperationException() {
        AcademicClass academicClass = new AcademicClass(CS2103T, F10_2);
        assertThrows(UnsupportedOperationException.class, () -> academicClass.getStudents().clear());
    }

    @Test
    public void isSameAcademicClass() {
        AcademicClass cs2103F10 = new AcademicClass(CS2103T, F10_2);

        // same object -> returns true
        assertTrue(cs2103F10.isSameAcademicClass(cs2103F10));

        // null -> returns false
        assertFalse(cs2103F10.isSameAcademicClass(null));

        // same normalized module code and class name -> returns true
        AcademicClass editedCs2103F10 = new AcademicClass(new ModuleCode("cs2103t"), new ClassName("f10-2"));
        assertTrue(cs2103F10.isSameAcademicClass(editedCs2103F10));

        // same module code, different class name -> returns false
        AcademicClass sameModuleDifferentClass = new AcademicClass(CS2103T, T24);
        assertFalse(cs2103F10.isSameAcademicClass(sameModuleDifferentClass));

        // different module code, same class name -> returns false
        AcademicClass differentModuleSameClass = new AcademicClass(ST2334, F10_2);
        assertFalse(cs2103F10.isSameAcademicClass(differentModuleSameClass));
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
        String expected = AcademicClass.class.getCanonicalName() + "{moduleCode=" + academicClass.getModuleCode()
                + ", className=" + academicClass.getClassName() + ", students=" + academicClass.getStudents() + "}";
        assertEquals(expected, academicClass.toString());
    }
}
