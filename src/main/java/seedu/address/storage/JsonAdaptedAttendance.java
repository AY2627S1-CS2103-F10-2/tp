package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/** JSON representation of attendance using student and class identifiers, not duplicated objects. */
class JsonAdaptedAttendance {
    private final String studentNumber;
    private final String moduleCode;
    private final String className;
    private final List<String> attendedDates;

    /** Creates an adapted record; missing fields are rejected during model conversion. */
    @JsonCreator
    public JsonAdaptedAttendance(@JsonProperty("studentNumber") String studentNumber,
            @JsonProperty("moduleCode") String moduleCode,
            @JsonProperty("className") String className,
            @JsonProperty("attendedDates") List<String> attendedDates) {
        this.studentNumber = studentNumber;
        this.moduleCode = moduleCode;
        this.className = className;
        this.attendedDates = attendedDates == null ? null : new ArrayList<>(attendedDates);
    }

    /** Copies the identifiers and chronological date values from a model association. */
    public JsonAdaptedAttendance(Attendance source) {
        studentNumber = source.getStudent().getStudentNumber().value;
        moduleCode = source.getAcademicClass().getModuleCode().value;
        className = source.getAcademicClass().getClassName().value;
        attendedDates = source.getAttendedDates().stream().map(LocalDate::toString).toList();
    }

    /** Resolves canonical endpoints and validates dates after students and classes have been loaded. */
    public Attendance toModelType(ReadOnlyAddressBook addressBook) throws IllegalValueException {
        if (studentNumber == null || moduleCode == null || className == null || attendedDates == null) {
            throw new IllegalValueException("Attendance record has a missing field.");
        }
        try {
            Person student = addressBook.findPersonByStudentNumber(new StudentNumber(studentNumber))
                    .orElseThrow(() -> new IllegalValueException("Attendance references a nonexistent student."));
            AcademicClass academicClass = addressBook.findAcademicClass(new ModuleCode(moduleCode),
                    new ClassName(className))
                    .orElseThrow(() -> new IllegalValueException("Attendance references a nonexistent class."));
            return Attendance.fromRecordedDates(student, academicClass, parseRecordedDates());
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException("Invalid attendance record: " + e.getMessage());
        }
    }

    private Set<LocalDate> parseRecordedDates() throws IllegalValueException {
        Set<LocalDate> dates = new TreeSet<>();
        for (String value : attendedDates) {
            if (value == null) {
                throw new IllegalValueException("Attendance date must not be null.");
            }
            try {
                LocalDate date = LocalDate.parse(value);
                if (!date.toString().equals(value)) {
                    throw new IllegalValueException("Attendance dates must use canonical ISO local-date format.");
                }
                if (!dates.add(date)) {
                    throw new IllegalValueException("Attendance contains a duplicate date.");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalValueException("Attendance contains an invalid calendar date.");
            }
        }
        return dates;
    }
}
