package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/**
 * Jackson-friendly version of {@link AcademicClass}.
 */
class JsonAdaptedAcademicClass {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Academic class's %s field is missing!";
    public static final String MESSAGE_INVALID_STUDENT_NUMBER = "Class membership contains an invalid student number.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Class membership references a nonexistent student: %s";
    public static final String MESSAGE_INVALID_ATTENDANCE = "Class attendance contains an invalid student or date.";

    private final String moduleCode;
    private final String className;
    private final int totalLessons;
    private final List<String> studentNumbers = new ArrayList<>();
    private final Map<String, List<String>> attendance = new HashMap<>();

    /**
     * Constructs a {@code JsonAdaptedAcademicClass} with the given academic class details.
     */
    public JsonAdaptedAcademicClass(String moduleCode, String className) {
        this(moduleCode, className, 0, null, null);
    }

    /**
     * Constructs a class with membership identifiers. Missing memberships default to an empty list.
     */
    @JsonCreator
    public JsonAdaptedAcademicClass(@JsonProperty("moduleCode") String moduleCode,
            @JsonProperty("className") String className,
            @JsonProperty("totalLessons") Integer totalLessons,
            @JsonProperty("studentNumbers") List<String> studentNumbers,
            @JsonProperty("attendance") Map<String, List<String>> attendance) {
        this.moduleCode = moduleCode;
        this.className = className;
        this.totalLessons = totalLessons == null ? 0 : totalLessons;
        if (studentNumbers != null) {
            this.studentNumbers.addAll(studentNumbers);
        }
        if (attendance != null) {
            attendance.forEach((studentNumber, dates) -> this.attendance.put(studentNumber,
                    dates == null ? new ArrayList<>() : new ArrayList<>(dates)));
        }
    }

    /** Backwards-compatible constructor for class files that only contain membership. */
    public JsonAdaptedAcademicClass(String moduleCode, String className, List<String> studentNumbers) {
        this(moduleCode, className, 0, studentNumbers, null);
    }

    /**
     * Converts a given {@code AcademicClass} into this class for Jackson use.
     */
    public JsonAdaptedAcademicClass(AcademicClass source) {
        moduleCode = source.getModuleCode().value;
        className = source.getClassName().value;
        totalLessons = source.getTotalLessons();
        source.getStudents().stream().map(person -> person.getStudentNumber().value)
                .sorted().forEach(studentNumbers::add);
        source.getAttendance().forEach((studentNumber, dates) -> attendance.put(studentNumber.value,
                dates.stream().map(LocalDate::toString).sorted().toList()));
    }

    /**
     * Restores memberships using the address book's stored students, after all students have been loaded.
     * Repeated identifiers result in a single membership.
     * @throws IllegalValueException if an identifier is invalid or refers to a missing student.
     */
    public void restoreStudents(AcademicClass academicClass, ReadOnlyAddressBook addressBook)
            throws IllegalValueException {
        for (String studentNumber : studentNumbers) {
            if (studentNumber == null || !StudentNumber.isValidStudentNumber(studentNumber)) {
                throw new IllegalValueException(MESSAGE_INVALID_STUDENT_NUMBER);
            }
            Person person = addressBook.findPersonByStudentNumber(new StudentNumber(studentNumber))
                    .orElseThrow(() -> new IllegalValueException(
                            String.format(MESSAGE_STUDENT_NOT_FOUND, studentNumber)));
            academicClass.assignStudent(person);
        }
        for (Map.Entry<String, List<String>> entry : attendance.entrySet()) {
            if (entry.getKey() == null || !StudentNumber.isValidStudentNumber(entry.getKey())) {
                throw new IllegalValueException(MESSAGE_INVALID_ATTENDANCE);
            }
            Person person = addressBook.findPersonByStudentNumber(new StudentNumber(entry.getKey()))
                    .filter(academicClass.getStudents()::contains)
                    .orElseThrow(() -> new IllegalValueException(MESSAGE_INVALID_ATTENDANCE));
            for (String date : entry.getValue()) {
                try {
                    if (!academicClass.addAttendance(person, LocalDate.parse(date))) {
                        throw new IllegalValueException(MESSAGE_INVALID_ATTENDANCE);
                    }
                } catch (DateTimeParseException | NullPointerException e) {
                    throw new IllegalValueException(MESSAGE_INVALID_ATTENDANCE);
                }
            }
        }
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
        if (totalLessons < 0) {
            throw new IllegalValueException("Total lessons cannot be negative.");
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

        return new AcademicClass(modelModuleCode, modelClassName, totalLessons);
    }
}
