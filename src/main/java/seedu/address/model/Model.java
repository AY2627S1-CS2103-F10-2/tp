package seedu.address.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;


/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /** {@code Predicate} that always evaluates to true for academic classes. */
    Predicate<AcademicClass> PREDICATE_SHOW_ALL_ACADEMIC_CLASSES = unused -> true;

    /** Returns snapshots with isolated date sets and shared canonical student/class references. */
    List<Attendance> getAttendanceList();

    /**
     * Records attendance for existing endpoints, returning false for a duplicate date.
     * Current-membership eligibility is checked by the command, not this storage API.
     * @throws IllegalArgumentException if an endpoint is missing or the date is in the future.
     * @throws NullPointerException if any argument is null.
     */
    boolean addAttendance(Person person, AcademicClass academicClass, LocalDate date);

    /**
     * Removes a recorded date, including history retained after unassignment.
     * Recorded dates remain removable after a clock rollback. Returns false for an unrecorded non-future date.
     * @throws IllegalArgumentException if an endpoint is missing or an unrecorded date is in the future.
     * @throws NullPointerException if any argument is null.
     */
    boolean removeAttendance(Person person, AcademicClass academicClass, LocalDate date);

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Finds the stored person by student number across the full address book, regardless of the current filter.
     * Returns an empty optional if no person matches. Does not change the current filter.
     * @throws NullPointerException if {@code studentNumber} is null.
     */
    Optional<Person> findPersonByStudentNumber(StudentNumber studentNumber);

    /** Returns the class whose attendance is currently shown beside the person list, if any. */
    Optional<AcademicClass> getAttendanceDisplayClass();


    /**
     * Returns true if a person with the same email (ignoring case) as {@code person} exists in the address book.
     */
    boolean hasEmail(Person person);

    /**
     * Returns true if a person with the same Telegram username (ignoring case) as {@code person} exists in the
     * address book.
     */
    boolean hasTelegram(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    void setAcademicClass(AcademicClass target, AcademicClass editedClass);

    /**
     * Returns true if an academic class with the same module code and class name exists in the address book.
     */
    boolean hasAcademicClass(AcademicClass academicClass);

    /**
     * Returns the class matching both identifiers, or an empty optional if no class matches.
     * @throws NullPointerException if either identifier is null.
     */
    Optional<AcademicClass> findAcademicClass(ModuleCode moduleCode, ClassName className);

    /**
     * Displays the given class's students alphabetically and tracks the selected class.
     *
     * @throws NullPointerException if {@code academicClass} is null
     */
    void filterPersonsByClass(AcademicClass academicClass);

    /**
     * Displays the given class's students alphabetically, optionally restricted to those with fewer than
     * {@code threshold} recorded attendance dates for that class, and tracks the selected class.
     *
     * @throws NullPointerException if {@code academicClass} or {@code threshold} is null
     * @throws IllegalArgumentException if {@code threshold} is present and not positive
     */
    void filterPersonsByClass(AcademicClass academicClass, Optional<Integer> threshold);

    /** Filters by class and sets that class as the attendance context in one refresh. */
    default void filterPersonsByClassAndDisplayAttendance(AcademicClass academicClass, Optional<Integer> threshold) {
        filterPersonsByClass(academicClass, threshold);
        setAttendanceDisplayClass(academicClass);
    }

    /**
     * Adds the given academic class.
     * {@code academicClass} must not already exist in the address book.
     */
    void addAcademicClass(AcademicClass academicClass);

    /**
     * Deletes the given academic class.
     * The academic class must exist in the address book.
     */
    void deleteAcademicClass(AcademicClass target);

    /**
     * Assigns the given student to the given class.
     * Both arguments must be the actual objects stored in this address book.
     * Returns true if membership was added, or false if already assigned.
     * Does not change the current person filter or displayed order.
     * @throws NullPointerException if either argument is null.
     */
    boolean assignStudentToClass(Person person, AcademicClass academicClass);

    /** Sets the class whose attendance should be rendered beside displayed students. */
    void setAttendanceDisplayClass(AcademicClass academicClass);

    /** Clears the attendance context shown beside displayed students. */
    void clearAttendanceDisplayClass();

    /**
     * Returns an unmodifiable view of the academic class list.
     */
    ObservableList<AcademicClass> getAcademicClassList();

    /** Returns an unmodifiable view of the filtered academic class list. */
    ObservableList<AcademicClass> getFilteredAcademicClassList();

    /** Returns an unmodifiable observable view of persons in their displayed order. */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the person filter and restores the matching persons' insertion order.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Updates the person filter and displays matching persons alphabetically by name.
     * Leaves the stored person records and their order unchanged.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonListAlphabetically(Predicate<Person> predicate);

    /** Refreshes the displayed person list without changing its filter or ordering. */
    default void refreshFilteredPersonList() {
        // Optional for model implementations that do not expose a live UI list.
    }

    /**
     * Updates the academic class filter while preserving insertion order.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredAcademicClassList(Predicate<AcademicClass> predicate);

    /**
     * Shows classes containing the selected student, preserving class insertion order.
     * Keeps this view updated after membership changes and edits to the student's number.
     * Leaves the displayed student list unchanged.
     * @throws NullPointerException if {@code studentNumber} is null.
     */
    void updateFilteredAcademicClassListByStudent(StudentNumber studentNumber);

    /**
     * Unassigns the stored student from the stored academic class with the given identities.
     * Does not change the current person filter or displayed order.
     *
     * @return true if membership was removed, or false if the student was not assigned
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if the student or academic class does not exist
     */
    boolean unassignStudentFromClass(Person person, AcademicClass academicClass);
}
