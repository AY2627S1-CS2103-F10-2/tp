package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_ACADEMIC_CLASS =
            "Academic classes list contains duplicate academic class(es).";

    private final List<String> commandHistory = new ArrayList<>();
    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedAcademicClass> classes = new ArrayList<>();
    private final List<JsonAdaptedAttendance> attendance = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and academic classes.
     */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons, List<JsonAdaptedAcademicClass> classes) {
        this(persons, classes, null);
    }

    /** Creates JSON data; absent attendance in older files defaults to an empty list. */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons,
            List<JsonAdaptedAcademicClass> classes,
            List<JsonAdaptedAttendance> attendance) {
        this(persons, classes, attendance, null);
    }

    /** Creates JSON data; older files default to empty command history. */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("classes") List<JsonAdaptedAcademicClass> classes,
            @JsonProperty("attendance") List<JsonAdaptedAttendance> attendance,
            @JsonProperty("commandHistory") List<String> commandHistory) {
        if (commandHistory != null) {
            this.commandHistory.addAll(commandHistory);
        }
        this.persons.addAll(persons);
        if (classes != null) {
            this.classes.addAll(classes);
        }
        if (attendance != null) {
            this.attendance.addAll(attendance);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        commandHistory.addAll(source.getCommandHistory());
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        classes.addAll(source.getAcademicClassList().stream()
                .map(JsonAdaptedAcademicClass::new)
                .collect(Collectors.toList()));
        attendance.addAll(source.getAttendanceList().stream().map(JsonAdaptedAttendance::new).toList());
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person) || addressBook.hasEmail(person) || addressBook.hasTelegram(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
        }
        for (JsonAdaptedAcademicClass jsonAdaptedAcademicClass : classes) {
            AcademicClass academicClass = jsonAdaptedAcademicClass.toModelType();
            if (addressBook.hasAcademicClass(academicClass)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_ACADEMIC_CLASS);
            }
            jsonAdaptedAcademicClass.restoreStudents(academicClass, addressBook);
            addressBook.addAcademicClass(academicClass);
        }
        restoreAttendance(addressBook);
        try {
            addressBook.setCommandHistory(commandHistory);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalValueException("Invalid command history: commands must be non-null and non-blank.");
        }
        return addressBook;
    }

    private void restoreAttendance(AddressBook addressBook) throws IllegalValueException {
        List<Attendance> records = new ArrayList<>();
        for (JsonAdaptedAttendance record : attendance) {
            if (record == null) {
                throw new IllegalValueException("Attendance record must not be null.");
            }
            records.add(record.toModelType(addressBook));
        }
        try {
            addressBook.setAttendance(records);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException("Invalid attendance associations: " + e.getMessage());
        }
    }

}
