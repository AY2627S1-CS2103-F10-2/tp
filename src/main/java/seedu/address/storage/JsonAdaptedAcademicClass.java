package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/** Jackson-friendly version of {@link AcademicClass}. */
class JsonAdaptedAcademicClass {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Academic class's %s field is missing!";

    private final String moduleCode;
    private final String className;

    @JsonCreator
    public JsonAdaptedAcademicClass(@JsonProperty("moduleCode") String moduleCode,
            @JsonProperty("className") String className) {
        this.moduleCode = moduleCode;
        this.className = className;
    }

    JsonAdaptedAcademicClass(AcademicClass source) {
        moduleCode = source.getModuleCode().value;
        className = source.getClassName().value;
    }

    AcademicClass toModelType() throws IllegalValueException {
        if (moduleCode == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ModuleCode.class.getSimpleName()));
        }
        if (!ModuleCode.isValidModuleCode(moduleCode)) {
            throw new IllegalValueException(ModuleCode.MESSAGE_CONSTRAINTS);
        }

        if (className == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ClassName.class.getSimpleName()));
        }
        if (!ClassName.isValidClassName(className)) {
            throw new IllegalValueException(ClassName.MESSAGE_CONSTRAINTS);
        }

        return new AcademicClass(new ModuleCode(moduleCode), new ClassName(className));
    }
}
