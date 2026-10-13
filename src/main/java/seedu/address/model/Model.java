package seedu.address.model;

import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;


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
     * Returns the class matching both identifiers, or an empty optional if no class matches.
     * @throws NullPointerException if either identifier is null.
     */
    Optional<AcademicClass> findAcademicClass(ModuleCode moduleCode, ClassName className);

    /**
     * Adds the given academic class.
     * {@code academicClass} must not already exist in the address book.
     */
    void addAcademicClass(AcademicClass academicClass);

    /**
     * Assigns the given student to the given class.
     * Both arguments must be the actual objects stored in this address book.
     * Returns true if membership was added, or false if already assigned.
     * Does not change the current person filter or displayed order.
     * @throws NullPointerException if either argument is null.
     */
    boolean assignStudentToClass(Person person, AcademicClass academicClass);

    /**
     * Returns an unmodifiable view of the academic class list.
     */
    ObservableList<AcademicClass> getAcademicClassList();

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
}
