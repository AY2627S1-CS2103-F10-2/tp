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
    private static final String INVALID_MODULE_CODE = "CS-2103";
    private static final String INVALID_CLASS_NAME = "T 24";

    private static final String VALID_MODULE_CODE = "CS2103";
    private static final String VALID_CLASS_NAME = "F10-2";

    @Test
    public void toModelType_validAcademicClassDetails_returnsAcademicClass() throws Exception {
        AcademicClass academicClass = new AcademicClass(new ModuleCode(VALID_MODULE_CODE),
                new ClassName(VALID_CLASS_NAME));
        JsonAdaptedAcademicClass jsonAdaptedAcademicClass = new JsonAdaptedAcademicClass(academicClass);
        assertEquals(academicClass, jsonAdaptedAcademicClass.toModelType());
    }

    @Test
    public void toModelType_normalizableAcademicClassDetails_returnsAcademicClass() throws Exception {
        AcademicClass academicClass = new AcademicClass(new ModuleCode(VALID_MODULE_CODE),
                new ClassName(VALID_CLASS_NAME));
        JsonAdaptedAcademicClass jsonAdaptedAcademicClass = new JsonAdaptedAcademicClass(" cs2103 ", " f10-2 ");
        assertEquals(academicClass, jsonAdaptedAcademicClass.toModelType());
    }

    @Test
    public void toModelType_invalidModuleCode_throwsIllegalValueException() {
        JsonAdaptedAcademicClass jsonAdaptedAcademicClass = new JsonAdaptedAcademicClass(INVALID_MODULE_CODE,
                VALID_CLASS_NAME);
        assertThrows(IllegalValueException.class, ModuleCode.MESSAGE_CONSTRAINTS,
                jsonAdaptedAcademicClass::toModelType);
    }

    @Test
    public void toModelType_nullModuleCode_throwsIllegalValueException() {
        JsonAdaptedAcademicClass jsonAdaptedAcademicClass = new JsonAdaptedAcademicClass(null, VALID_CLASS_NAME);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ModuleCode.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, jsonAdaptedAcademicClass::toModelType);
    }

    @Test
    public void toModelType_invalidClassName_throwsIllegalValueException() {
        JsonAdaptedAcademicClass jsonAdaptedAcademicClass = new JsonAdaptedAcademicClass(VALID_MODULE_CODE,
                INVALID_CLASS_NAME);
        assertThrows(IllegalValueException.class, ClassName.MESSAGE_CONSTRAINTS,
                jsonAdaptedAcademicClass::toModelType);
    }

    @Test
    public void toModelType_nullClassName_throwsIllegalValueException() {
        JsonAdaptedAcademicClass jsonAdaptedAcademicClass = new JsonAdaptedAcademicClass(VALID_MODULE_CODE, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ClassName.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, jsonAdaptedAcademicClass::toModelType);
    }
}
