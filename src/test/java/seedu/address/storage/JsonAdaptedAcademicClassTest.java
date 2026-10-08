package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedAcademicClass.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

public class JsonAdaptedAcademicClassTest {

    private static final AcademicClass VALID_CLASS =
            new AcademicClass(new ModuleCode("CS2100"), new ClassName("L03"));

    @Test
    public void toModelType_validAcademicClassDetails_returnsAcademicClass() throws Exception {
        JsonAdaptedAcademicClass academicClass = new JsonAdaptedAcademicClass(VALID_CLASS);
        assertEquals(VALID_CLASS, academicClass.toModelType());
    }

    @Test
    public void toModelType_nullModuleCode_throwsIllegalValueException() {
        JsonAdaptedAcademicClass academicClass = new JsonAdaptedAcademicClass(null, VALID_CLASS.getClassName().value);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ModuleCode.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, academicClass::toModelType);
    }

    @Test
    public void toModelType_invalidModuleCode_throwsIllegalValueException() {
        JsonAdaptedAcademicClass academicClass = new JsonAdaptedAcademicClass("2100", VALID_CLASS.getClassName().value);
        assertThrows(IllegalValueException.class, ModuleCode.MESSAGE_CONSTRAINTS, academicClass::toModelType);
    }

    @Test
    public void toModelType_nullClassName_throwsIllegalValueException() {
        JsonAdaptedAcademicClass academicClass = new JsonAdaptedAcademicClass(
                VALID_CLASS.getModuleCode().value, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ClassName.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, academicClass::toModelType);
    }

    @Test
    public void toModelType_invalidClassName_throwsIllegalValueException() {
        JsonAdaptedAcademicClass academicClass = new JsonAdaptedAcademicClass(
                VALID_CLASS.getModuleCode().value, "---");
        assertThrows(IllegalValueException.class, ClassName.MESSAGE_CONSTRAINTS, academicClass::toModelType);
    }
}
