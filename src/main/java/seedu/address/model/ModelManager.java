package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonNameComparator;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private final ObservableList<Person> displayedPersons;
    private final FilteredList<AcademicClass> filteredAcademicClasses;
    private final ObservableList<AcademicClass> displayedAcademicClasses;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons);
        displayedPersons = FXCollections.unmodifiableObservableList(sortedPersons);
        filteredAcademicClasses = new FilteredList<>(this.addressBook.getAcademicClassList());
        displayedAcademicClasses = FXCollections.unmodifiableObservableList(filteredAcademicClasses);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public List<Attendance> getAttendanceList() {
        return addressBook.getAttendanceList();
    }

    @Override
    public boolean addAttendance(Person person, AcademicClass academicClass, LocalDate date) {
        return addressBook.addAttendance(person, academicClass, date);
    }

    @Override
    public boolean removeAttendance(Person person, AcademicClass academicClass, LocalDate date) {
        return addressBook.removeAttendance(person, academicClass, date);
    }

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }


    @Override
    public boolean hasEmail(Person person) {
        requireNonNull(person);
        return addressBook.hasEmail(person);
    }

    @Override
    public boolean hasTelegram(Person person) {
        requireNonNull(person);
        return addressBook.hasTelegram(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public boolean hasAcademicClass(AcademicClass academicClass) {
        requireNonNull(academicClass);
        return addressBook.hasAcademicClass(academicClass);
    }

    @Override
    public Optional<AcademicClass> findAcademicClass(ModuleCode moduleCode, ClassName className) {
        return addressBook.findAcademicClass(moduleCode, className);
    }

    @Override
    public void addAcademicClass(AcademicClass academicClass) {
        addressBook.addAcademicClass(academicClass);
    }

    @Override
    public void deleteAcademicClass(AcademicClass target) {
        addressBook.removeAcademicClass(target);
    }

    @Override
    public boolean assignStudentToClass(Person person, AcademicClass academicClass) {
        return addressBook.assignStudentToClass(person, academicClass);
    }

    @Override
    public ObservableList<AcademicClass> getAcademicClassList() {
        return addressBook.getAcademicClassList();
    }

    @Override
    public ObservableList<AcademicClass> getFilteredAcademicClassList() {
        return displayedAcademicClasses;
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable observable view shared by the UI and index-based commands.
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return displayedPersons;
    }


    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
        sortedPersons.setComparator(null);
    }

    @Override
    public void updateFilteredPersonListAlphabetically(Predicate<Person> predicate) {
        updateFilteredPersonList(predicate);
        sortedPersons.setComparator(new PersonNameComparator());
    }

    @Override
    public void updateFilteredAcademicClassList(Predicate<AcademicClass> predicate) {
        requireNonNull(predicate);
        filteredAcademicClasses.setPredicate(predicate);
    }

    @Override
    public boolean unassignStudentFromClass(Person person, AcademicClass academicClass) {
        return addressBook.unassignStudentFromClass(person, academicClass);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && displayedPersons.equals(otherModelManager.displayedPersons)
                && displayedAcademicClasses.equals(otherModelManager.displayedAcademicClasses);
    }

}
