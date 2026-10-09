package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Jackson-friendly version of {@link AcademicClass}.
 */
class JsonAdaptedAcademicClass {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Academic class's %s field is missing!";

    private final String moduleCode;
    private final String className;

    /**
     * Constructs a {@code JsonAdaptedAcademicClass} with the given academic class details.
     */
    @JsonCreator
    public JsonAdaptedAcademicClass(@JsonProperty("moduleCode") String moduleCode,
            @JsonProperty("className") String className) {
        this.moduleCode = moduleCode;
        this.className = className;
    }

    /**
     * Converts a given {@code AcademicClass} into this class for Jackson use.
     */
    public JsonAdaptedAcademicClass(AcademicClass source) {
        moduleCode = source.getModuleCode().value;
        className = source.getClassName().value;
    }

    /**
     * Converts this Jackson-friendly adapted academic class object into the model's {@code AcademicClass} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted academic class.
     */
    public AcademicClass toModelType() throws IllegalValueException {
        if (moduleCode == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ModuleCode.class.getSimpleName()));
        }
        if (moduleCode.trim().isEmpty()) {
            throw new IllegalValueException(ModuleCode.MESSAGE_EMPTY);
        }
        if (!ModuleCode.isValidModuleCode(moduleCode.trim())) {
            throw new IllegalValueException(ModuleCode.MESSAGE_CONSTRAINTS);
        }
        final ModuleCode modelModuleCode = new ModuleCode(moduleCode);

        if (className == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ClassName.class.getSimpleName()));
        }
        if (className.trim().isEmpty()) {
            throw new IllegalValueException(ClassName.MESSAGE_EMPTY);
        }
        if (!ClassName.isValidClassName(className.trim())) {
            throw new IllegalValueException(ClassName.MESSAGE_CONSTRAINTS);
        }
        final ClassName modelClassName = new ClassName(className);

        return new AcademicClass(modelModuleCode, modelClassName);
    }
}
