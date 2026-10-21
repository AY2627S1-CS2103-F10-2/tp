package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

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
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonNameComparator;
import seedu.address.model.person.StudentNumber;

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
    private Predicate<Person> personFilter = PREDICATE_SHOW_ALL_PERSONS;
    private final FilteredList<AcademicClass> filteredAcademicClasses;
    private final ObservableList<AcademicClass> displayedAcademicClasses;
    private Predicate<AcademicClass> academicClassFilter = PREDICATE_SHOW_ALL_ACADEMIC_CLASSES;
    // Tracks the selected student's identity independently of changes to the displayed student index.
    private StudentNumber classFilterStudentNumber;

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
    public Optional<Person> findPersonByStudentNumber(StudentNumber studentNumber) {
        return addressBook.findPersonByStudentNumber(studentNumber);
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
        refreshFilteredAcademicClassList();
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
        if (target.getStudentNumber().equals(classFilterStudentNumber)) {
            classFilterStudentNumber = editedPerson.getStudentNumber();
        }
        refreshFilteredAcademicClassList();
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
        refreshFilteredPersonList();
    }

    @Override
    public boolean assignStudentToClass(Person person, AcademicClass academicClass) {
        boolean isAssigned = addressBook.assignStudentToClass(person, academicClass);
        if (isAssigned) {
            refreshFilteredPersonList();
            refreshFilteredAcademicClassList();
        }
        return isAssigned;
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
        personFilter = predicate;
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
        classFilterStudentNumber = null;
        academicClassFilter = predicate;
        filteredAcademicClasses.setPredicate(predicate);
    }

    @Override
    public void updateFilteredAcademicClassListByStudent(StudentNumber studentNumber) {
        requireNonNull(studentNumber);
        classFilterStudentNumber = studentNumber;
        academicClassFilter = academicClass -> academicClass.getStudents().stream()
                .anyMatch(student -> student.hasStudentNumber(classFilterStudentNumber));
        filteredAcademicClasses.setPredicate(academicClassFilter);
    }

    @Override
    public boolean unassignStudentFromClass(Person person, AcademicClass academicClass) {
        boolean isUnassigned = addressBook.unassignStudentFromClass(person, academicClass);
        if (isUnassigned) {
            refreshFilteredPersonList();
            refreshFilteredAcademicClassList();
        }
        return isUnassigned;
    }

    /**
     * Re-evaluates membership-dependent predicates without changing the current display order.
     */
    private void refreshFilteredPersonList() {
        // Wrap the original predicate, not the previous wrapper, to avoid accumulating nested calls.
        Predicate<Person> predicate = personFilter;
        filteredPersons.setPredicate(predicate::test);
    }

    /** Re-evaluates class membership filters after student or assignment changes. */
    private void refreshFilteredAcademicClassList() {
        Predicate<AcademicClass> predicate = academicClassFilter;
        filteredAcademicClasses.setPredicate(predicate::test);
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
