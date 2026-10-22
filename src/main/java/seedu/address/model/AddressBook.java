package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.academicclass.UniqueAcademicClassList;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;
import seedu.address.model.person.StudentObserver;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicate persons and academic classes are not allowed.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueAcademicClassList academicClasses = new UniqueAcademicClassList();
    private final List<Attendance> attendanceRecords = new ArrayList<>();

    private List<StudentObserver> studentObservers = new ArrayList<>();

    /**
     * Creates an empty AddressBook.
     */
    public AddressBook() {
    }

    /**
     * Creates an AddressBook using the persons and academic classes in
     * {@code toBeCopied}.
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);

        for (StudentObserver studentObserver : studentObservers) {
            studentObserver.onStudentListReplaced(persons);
        }
        reconcileAttendance();
    }

    /**
     * Replaces the contents of the academic class list.
     * {@code academicClasses} must not contain nulls or duplicate classes.
     */
    public void setAcademicClasses(List<AcademicClass> academicClasses) {
        this.academicClasses.setAcademicClasses(academicClasses);

        resetStudentObservers(academicClasses);
        reconcileAttendance();
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     * Both lists are validated before either existing list is modified.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        UniquePersonList replacementPersons = new UniquePersonList();
        replacementPersons.setPersons(newData.getPersonList());
        UniqueAcademicClassList replacementClasses = new UniqueAcademicClassList();
        replacementClasses.setAcademicClasses(newData.getAcademicClassList());
        List<Attendance> replacementAttendance = copyAttendance(newData);

        resetStudentObservers(newData.getAcademicClassList());

        persons.setPersons(replacementPersons);
        academicClasses.setAcademicClasses(replacementClasses.asUnmodifiableObservableList());
        attendanceRecords.clear();
        attendanceRecords.addAll(replacementAttendance);
    }

    private void resetStudentObservers(Iterable<AcademicClass> academicClasses) {
        studentObservers = new ArrayList<>();

        for (AcademicClass academicClass : academicClasses) {
            addStudentObserver(academicClass);
        }
    }

    /**
     * Removes the given observer so it no longer receives student change notifications.
     *
     * TODO: When implementing DeleteClassCommand, ensure the model's class deletion method
     * calls this method with the deleted class's stored instance after successful removal.
     *
     * @throws NullPointerException if {@code studentObserver} is null
     */
    private void removeStudentObserver(StudentObserver studentObserver) {
        requireNonNull(studentObserver);
        this.studentObservers.remove(studentObserver);
    }

    private void addStudentObserver(StudentObserver studentObserver) {
        requireNonNull(studentObserver);
        this.studentObservers.add(studentObserver);
    }

    private void updateStudentObservers(Person oldStudent, Person newStudent) {
        for (StudentObserver studentObserver : studentObservers) {
            studentObserver.onStudentUpdate(oldStudent, newStudent);
        }
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in
     * the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Returns true if a person with the same email (ignoring case) as
     * {@code person} exists in the address book.
     */
    public boolean hasEmail(Person person) {
        requireNonNull(person);
        return persons.containsEmail(person);
    }

    /**
     * Returns true if a person with the same Telegram username (ignoring case) as
     * {@code person} exists in the
     * address book.
     */
    public boolean hasTelegram(Person person) {
        requireNonNull(person);
        return persons.containsTelegram(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    @Override
    public Optional<AcademicClass> findAcademicClass(ModuleCode moduleCode, ClassName className) {
        requireNonNull(moduleCode);
        requireNonNull(className);
        return this.getAcademicClassList().stream().filter(
                c -> c.hasIdentity(moduleCode, className)).findFirst();
    }

    @Override
    public Optional<Person> findPersonByStudentNumber(StudentNumber studentNumber) {
        requireNonNull(studentNumber);
        return this.getPersonList().stream()
                .filter(person -> person.hasStudentNumber(studentNumber))
                .findFirst();
    }

    /**
     * Replaces the given person {@code target} in the list with
     * {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another
     * existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
        updateStudentObservers(target, editedPerson);
        attendanceRecords.replaceAll(record -> record.getStudent().isSamePerson(target)
                ? record.copyFor(editedPerson, record.getAcademicClass()) : record);
    }

    public void setAcademicClass(AcademicClass target, AcademicClass editedClass) {
        requireNonNull(editedClass);
        academicClasses.setAcademicClass(target, editedClass);
        removeStudentObserver(target);
        addStudentObserver(editedClass);

        replaceOldAttendanceClassWithNew(target, editedClass);
    }

    /**
     * Rebinds all attendance history for {@code oldClass} to {@code newClass}, preserving students and dates.
     * This includes history for students who are no longer assigned to the class.
     */
    private void replaceOldAttendanceClassWithNew(AcademicClass oldClass, AcademicClass newClass) {
        attendanceRecords.replaceAll(attendance ->
                attendance.getAcademicClass().equals(oldClass)
                        ? attendance.copyFor(attendance.getStudent(), newClass)
                        : attendance);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
        updateStudentObservers(key, null);
        attendanceRecords.removeIf(record -> record.getStudent().isSamePerson(key));
    }

    //// academic class-level operations

    /**
     * Returns true if an academic class with the same module code and class name exists in the address book.
     */
    public boolean hasAcademicClass(AcademicClass academicClass) {
        requireNonNull(academicClass);
        return academicClasses.asUnmodifiableObservableList().contains(academicClass);
    }

    /**
     * Adds an academic class to the address book.
     * The academic class must not already exist in the address book.
     */
    public void addAcademicClass(AcademicClass academicClass) {
        requireNonNull(academicClass);
        List<AcademicClass> updatedAcademicClasses = new ArrayList<>(academicClasses.asUnmodifiableObservableList());
        updatedAcademicClasses.add(academicClass);
        academicClasses.setAcademicClasses(updatedAcademicClasses);

        studentObservers.add(academicClass);
    }

    /**
     * Removes {@code key} from this address book without deleting any person records.
     * {@code key} must exist in the address book.
     */
    public void removeAcademicClass(AcademicClass key) {
        academicClasses.remove(key);
        removeStudentObserver(key);
        attendanceRecords.removeIf(record -> record.getAcademicClass().equals(key));
    }

    /**
     * Assigns the given student to the given class.
     * Both arguments must be the actual objects stored in this address book.
     * Returns true if membership was added, or false if already assigned.
     *
     * @throws NullPointerException     if either argument is null.
     */
    public boolean assignStudentToClass(Person person, AcademicClass academicClass) {
        requireAllNonNull(person, academicClass);
        assert findPersonByStudentNumber(person.getStudentNumber())
                .map(stored -> stored == person)
                .orElse(false)
                : "Student must be the actual object stored in this address book";
        assert findAcademicClass(academicClass.getModuleCode(), academicClass.getClassName())
                .map(stored -> stored == academicClass)
                .orElse(false)
                : "Class must be the actual object stored in this address book";
        return academicClass.assignStudent(person);
    }

    /**
     * Unassigns the stored student from the stored academic class with the given identities.
     *
     * @return true if membership was removed, or false if the student was not assigned
     * @throws NullPointerException if either argument is null
     */
    public boolean unassignStudentFromClass(Person person, AcademicClass academicClass) {
        requireAllNonNull(person, academicClass);
        return academicClass.unassignStudent(person);
    }

    //// attendance operations

    /** Replaces attendance after validating all associations, without partially changing existing history. */
    public void setAttendance(List<Attendance> records) {
        requireNonNull(records);
        List<Attendance> replacement = copyAttendance(records, this);
        attendanceRecords.clear();
        attendanceRecords.addAll(replacement);
    }

    @Override
    public List<Attendance> getAttendanceList() {
        return attendanceRecords.stream()
                .map(record -> record.copyFor(record.getStudent(), record.getAcademicClass())).toList();
    }

    /** Returns attendance date counts for one class without copying the records or their dates. */
    public Map<StudentNumber, Integer> getAttendanceCounts(AcademicClass academicClass) {
        requireNonNull(academicClass);
        Map<StudentNumber, Integer> counts = new HashMap<>();
        for (Attendance record : attendanceRecords) {
            if (record.getAcademicClass().isSameClass(academicClass)) {
                counts.put(record.getStudent().getStudentNumber(), record.getAttendanceCount());
            }
        }
        return counts;
    }

    /** Records a valid date against canonical stored endpoints; membership eligibility is caller policy. */
    public boolean addAttendance(Person person, AcademicClass academicClass, LocalDate date) {
        requireAllNonNull(person, academicClass, date);
        Person storedPerson = requireStoredPerson(person);
        AcademicClass storedClass = requireStoredClass(academicClass);
        Optional<Attendance> existing = findAttendance(storedPerson, storedClass);
        if (existing.isPresent()) {
            return existing.get().addDate(date);
        }
        Attendance record = new Attendance(storedPerson, storedClass);
        record.addDate(date);
        attendanceRecords.add(record);
        return true;
    }

    /** Removes a recorded date without requiring current membership; returns false if it is absent. */
    public boolean removeAttendance(Person person, AcademicClass academicClass, LocalDate date) {
        requireAllNonNull(person, academicClass, date);
        Person storedPerson = requireStoredPerson(person);
        AcademicClass storedClass = requireStoredClass(academicClass);
        Attendance record = findAttendance(storedPerson, storedClass)
                .orElseGet(() -> new Attendance(storedPerson, storedClass));
        return record.removeDate(date);
    }

    private Optional<Attendance> findAttendance(Person person, AcademicClass academicClass) {
        return attendanceRecords.stream().filter(record -> record.getStudent().isSamePerson(person)
                && record.getAcademicClass().equals(academicClass)).findFirst();
    }

    private Person requireStoredPerson(Person person) {
        return findPersonByStudentNumber(person.getStudentNumber())
                .orElseThrow(() -> new IllegalArgumentException("Attendance student does not exist."));
    }

    private AcademicClass requireStoredClass(AcademicClass academicClass) {
        return findAcademicClass(academicClass.getModuleCode(), academicClass.getClassName())
                .orElseThrow(() -> new IllegalArgumentException("Attendance class does not exist."));
    }

    /** Rebinds retained identities after bulk replacement and removes history for deleted endpoints. */
    private void reconcileAttendance() {
        List<Attendance> retained = new ArrayList<>();
        for (Attendance record : attendanceRecords) {
            Optional<Person> person = findPersonByStudentNumber(record.getStudent().getStudentNumber());
            AcademicClass oldClass = record.getAcademicClass();
            Optional<AcademicClass> academicClass = findAcademicClass(
                    oldClass.getModuleCode(), oldClass.getClassName());
            if (person.isPresent() && academicClass.isPresent()) {
                retained.add(record.copyFor(person.get(), academicClass.get()));
            }
        }
        attendanceRecords.clear();
        attendanceRecords.addAll(retained);
    }

    /** Validates and copies associations before reset mutates any existing data. */
    private List<Attendance> copyAttendance(ReadOnlyAddressBook source) {
        return copyAttendance(source.getAttendanceList(), source);
    }

    private List<Attendance> copyAttendance(List<Attendance> records, ReadOnlyAddressBook source) {
        List<Attendance> copies = new ArrayList<>();
        for (Attendance record : records) {
            requireNonNull(record);
            Person person = source.getPersonList().stream()
                    .filter(candidate -> candidate.isSamePerson(record.getStudent())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Attendance student does not exist."));
            AcademicClass academicClass = source.getAcademicClassList().stream()
                    .filter(candidate -> candidate.equals(record.getAcademicClass())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Attendance class does not exist."));
            boolean isDuplicate = copies.stream().anyMatch(copy -> copy.getStudent().isSamePerson(person)
                    && copy.getAcademicClass().equals(academicClass));
            if (isDuplicate) {
                throw new IllegalArgumentException("Duplicate attendance association.");
            }
            copies.add(record.copyFor(person, academicClass));
        }
        return copies;
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("academicClasses", academicClasses)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<AcademicClass> getAcademicClassList() {
        return academicClasses.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && academicClasses.equals(otherAddressBook.academicClasses)
                && attendanceRecords.equals(otherAddressBook.attendanceRecords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, academicClasses, attendanceRecords);
    }
}
