package seedu.address.model;

import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

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
     * Finds a person by their exact student number in the full address book, regardless of the current filter.
     * Returns an empty Optional if no matching person exists. Does not change the current filter.
     * @throws NullPointerException if {@code studentNumber} is null.
     */
    Optional<Person> findPersonByStudentNumber(StudentNumber studentNumber);

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

    /**
     * Returns true if an academic class with the same module code and class name exists in the address book.
     */
    boolean hasAcademicClass(AcademicClass academicClass);

    /**
     * Adds the given academic class.
     * {@code academicClass} must not already exist in the address book.
     */
    void addAcademicClass(AcademicClass academicClass);

    /**
     * Returns an unmodifiable view of the academic class list.
     */
    ObservableList<AcademicClass> getAcademicClassList();

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns the academic class with the given module code and class name.
     *
     * @param moduleCode the module code of the academic class to find
     * @param className the class name of the academic class to find
     * @return an {@code Optional} containing the matching academic class, or
     *         {@code Optional#empty()} if no matching class exists
     * @throws NullPointerException if {@code moduleCode} or {@code className} is null
     */
    Optional<AcademicClass> findAcademicClass(ModuleCode moduleCode, ClassName className);

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Updates the person filter and displays matching persons alphabetically by name.
     * Leaves the stored person records and their order unchanged.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonListAlphabetically(Predicate<Person> predicate);
}
