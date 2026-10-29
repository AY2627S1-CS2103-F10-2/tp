package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
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
    // Filter predicates are restored as-is on rollback, so they must capture
    // immutable values only.
    private Predicate<Person> personFilter =PREDICATE_SHOW_ALL_PERSONS;
    private AcademicClass personFilterClass;
    private Optional<Integer> personFilterThreshold = Optional.empty();
    private Map<StudentNumber, Integer> personFilterAttendanceCounts = Map.of();
    private final FilteredList<AcademicClass> filteredAcademicClasses;
    private final ObservableList<AcademicClass> displayedAcademicClasses;
    private AcademicClass attendanceDisplayClass;
    // Filter predicates are restored as-is on rollback, so they must capture
    // immutable values only.
    private Predicate<AcademicClass> academicClassFilter =PREDICATE_SHOW_ALL_ACADEMIC_CLASSES;
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
    public List<Attendance> getAttendanceList() {
        return addressBook.getAttendanceList();
    }

    @Override
    public boolean addAttendance(Person person, AcademicClass academicClass, LocalDate date) {
        boolean added = addressBook.addAttendance(person, academicClass, date);
        logger.info((added ? "Added" : "Skipped duplicate") + " attendance for class "
                + academicClass.getModuleCode() + " (" + academicClass.getClassName() + ")");
        if (added) {
            refreshFilteredPersonList();
        }
        return added;
    }

    @Override
    public boolean removeAttendance(Person person, AcademicClass academicClass, LocalDate date) {
        boolean removed = addressBook.removeAttendance(person, academicClass, date);
        logger.info((removed ? "Removed" : "Could not remove") + " attendance for class "
                + academicClass.getModuleCode() + " (" + academicClass.getClassName() + ")");
        if (removed) {
            refreshFilteredPersonList();
        }
        return removed;
    }

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        updateFilteredAcademicClassList(PREDICATE_SHOW_ALL_ACADEMIC_CLASSES);
        logger.fine("Replaced address book and reset student and class filters");
    }

    @Override
    public List<String> getCommandHistory() {
        return addressBook.getCommandHistory();
    }

    @Override
    public void setCommandHistory(List<String> commands) {
        addressBook.setCommandHistory(commands);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    private record DisplayState(Predicate<Person> personFilter, AcademicClass personFilterClass,
            Optional<Integer> personFilterThreshold, Comparator<? super Person> comparator,
            AcademicClass attendanceDisplayClass, Predicate<AcademicClass> academicClassFilter,
            StudentNumber classFilterStudentNumber) {}

    @Override
    public Rollback createRollback() {
        ReadOnlyAddressBook savedData = addressBook.deepCopy();
        DisplayState savedDisplay = new DisplayState(personFilter, personFilterClass, personFilterThreshold,
                sortedPersons.getComparator(), attendanceDisplayClass, academicClassFilter,
                classFilterStudentNumber);
        return () -> restore(savedData, savedDisplay);
    }

    private void restore(ReadOnlyAddressBook savedData, DisplayState state) {
        this.addressBook.resetData(savedData);
        // resetData replaces class objects, so look the saved classes up again by identity.
        personFilterClass = findStoredClass(state.personFilterClass());
        attendanceDisplayClass = findStoredClass(state.attendanceDisplayClass());
        personFilter = state.personFilter();
        personFilterThreshold = state.personFilterThreshold();
        academicClassFilter = state.academicClassFilter();
        classFilterStudentNumber = state.classFilterStudentNumber();
        sortedPersons.setComparator(state.comparator());
        refreshFilteredPersonList();
        refreshFilteredAcademicClassList();
        logger.fine("Restored address book and previous student and class display");
    }

    private AcademicClass findStoredClass(AcademicClass academicClass) {
        if (academicClass == null) {
            return null;
        }
        return addressBook.findAcademicClass(academicClass.getModuleCode(), academicClass.getClassName())
                .orElse(academicClass);
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
    public Optional<AcademicClass> getAttendanceDisplayClass() {
        return Optional.ofNullable(attendanceDisplayClass);
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
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
        refreshFilteredPersonList();
        if (target.getStudentNumber().equals(classFilterStudentNumber)) {
            classFilterStudentNumber = editedPerson.getStudentNumber();
            logger.fine(() -> "Updated selected student for class filter from " + target.getStudentNumber()
                    + " to " + editedPerson.getStudentNumber());
        }
        refreshFilteredAcademicClassList();
    }

    @Override
    public void setAcademicClass(AcademicClass target, AcademicClass editedClass) {
        requireAllNonNull(target, editedClass);

        boolean isViewingTargetClass = personFilterClass != null
                && personFilterClass.isSameClass(target);
        Optional<Integer> activeThreshold = personFilterThreshold;
        boolean isDisplayingTargetClass = attendanceDisplayClass != null
                && attendanceDisplayClass.isSameClass(target);

        addressBook.setAcademicClass(target, editedClass);

        if (isViewingTargetClass && isDisplayingTargetClass) {
            filterPersonsByClassAndDisplayAttendance(editedClass, activeThreshold);
        } else if (isViewingTargetClass) {
            filterPersonsByClass(editedClass, activeThreshold);
        } else if (isDisplayingTargetClass) {
            setAttendanceDisplayClass(editedClass);
        }

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
    public void filterPersonsByClass(AcademicClass academicClass) {
        filterPersonsByClass(academicClass, Optional.empty());
    }

    @Override
    public void filterPersonsByClass(AcademicClass academicClass, Optional<Integer> threshold) {
        applyClassFilter(academicClass, threshold, false);
    }

    @Override
    public void filterPersonsByClassAndDisplayAttendance(AcademicClass academicClass, Optional<Integer> threshold) {
        applyClassFilter(academicClass, threshold, true);
    }

    private void applyClassFilter(AcademicClass academicClass, Optional<Integer> threshold,
            boolean displayAttendance) {
        requireAllNonNull(academicClass, threshold);
        if (threshold.isPresent() && threshold.get() <= 0) {
            throw new IllegalArgumentException("Attendance threshold must be a positive integer.");
        }
        personFilterAttendanceCounts = threshold.isPresent()
                ? addressBook.getAttendanceCounts(academicClass) : Map.of();
        personFilterClass = academicClass;
        personFilterThreshold = threshold;
        attendanceDisplayClass = displayAttendance ? academicClass : null;
        personFilter = person -> addressBook
                .findAcademicClass(academicClass.getModuleCode(), academicClass.getClassName())
                .map(storedClass -> isClassMemberWithinThreshold(person, storedClass, threshold)).orElse(false);
        filteredPersons.setPredicate(personFilter);
        sortedPersons.setComparator(new PersonNameComparator());
    }

    private boolean isClassMemberWithinThreshold(Person person, AcademicClass storedClass,
            Optional<Integer> threshold) {
        boolean isMember = storedClass.getStudents().stream()
                .anyMatch(member -> member.isSamePerson(person));
        if (!isMember) {
            return false;
        }
        return threshold.map(limit -> personFilterAttendanceCounts
                .getOrDefault(person.getStudentNumber(), 0) < limit).orElse(true);
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
            logger.fine(() -> "Assigned student " + person.getStudentNumber() + " to class " + academicClass);
        }
        return isAssigned;
    }

    @Override
    public void setAttendanceDisplayClass(AcademicClass academicClass) {
        requireNonNull(academicClass);
        attendanceDisplayClass = academicClass;
        logger.info("Displaying attendance for class " + academicClass.getModuleCode() + " ("
                + academicClass.getClassName() + ")");
        refreshFilteredPersonList();
    }

    @Override
    public void clearAttendanceDisplayClass() {
        attendanceDisplayClass = null;
        logger.fine("Cleared attendance display class");
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
    public Optional<Person> findDisplayedPerson(Index index) {
        requireNonNull(index);
        int position = index.getZeroBased();
        return position < displayedPersons.size() ? Optional.of(displayedPersons.get(position)) : Optional.empty();
    }

    @Override
    public Optional<AcademicClass> findDisplayedAcademicClass(Index index) {
        requireNonNull(index);
        int position = index.getZeroBased();
        return position < displayedAcademicClasses.size()
                ? Optional.of(displayedAcademicClasses.get(position)) : Optional.empty();
    }


    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        clearAttendanceDisplayClass();
        this.personFilterClass = null;
        this.personFilterThreshold = Optional.empty();
        this.personFilterAttendanceCounts = Map.of();
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
        logger.fine(() -> "Updated class filter and cleared selected student; showing "
                + filteredAcademicClasses.size() + " classes");
    }

    @Override
    public void updateFilteredAcademicClassListByStudent(StudentNumber studentNumber) {
        requireNonNull(studentNumber);
        classFilterStudentNumber = studentNumber;
        academicClassFilter = academicClass -> academicClass.getStudents().stream()
                .anyMatch(student -> student.hasStudentNumber(classFilterStudentNumber));
        filteredAcademicClasses.setPredicate(academicClassFilter);
        logger.fine(() -> "Applied class filter for student " + studentNumber + "; showing "
                + filteredAcademicClasses.size() + " classes");
    }

    @Override
    public boolean unassignStudentFromClass(Person person, AcademicClass academicClass) {
        boolean isUnassigned = addressBook.unassignStudentFromClass(person, academicClass);
        if (isUnassigned) {
            refreshFilteredPersonList();
            refreshFilteredAcademicClassList();
            logger.fine(() -> "Unassigned student " + person.getStudentNumber() + " from class " + academicClass);
        }
        return isUnassigned;
    }

    /**
     * Re-evaluates membership-dependent predicates without changing the current display order.
     */
    @Override
    public void refreshFilteredPersonList() {
        if (personFilterClass != null && personFilterThreshold.isPresent()) {
            personFilterAttendanceCounts = addressBook.getAttendanceCounts(personFilterClass);
        }
        // Wrap the original predicate, not the previous wrapper, to avoid accumulating nested calls.
        Predicate<Person> predicate = personFilter;
        filteredPersons.setPredicate(predicate::test);
    }

    /** Re-evaluates class membership filters after student or assignment changes. */
    private void refreshFilteredAcademicClassList() {
        Predicate<AcademicClass> predicate = academicClassFilter;
        filteredAcademicClasses.setPredicate(predicate::test);
        if (classFilterStudentNumber != null) {
            logger.fine(() -> "Refreshed class filter for student " + classFilterStudentNumber + "; showing "
                    + filteredAcademicClasses.size() + " classes");
        }
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
