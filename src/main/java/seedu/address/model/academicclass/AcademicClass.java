package seedu.address.model.academicclass;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;
import seedu.address.model.person.StudentObserver;

/**
 * Represents a Class in the address book.
 * Guarantees: identity fields are immutable and validated; student membership can be updated.
 */
public class AcademicClass implements StudentObserver {

    // Identity fields
    private final ModuleCode moduleCode;
    private final ClassName className;
    private final int totalLessons;

    // Data fields
    private final ObservableSet<Person> students = FXCollections.observableSet(new HashSet<>());
    // Cached read-only view of students; reflects membership changes and forwards change notifications.
    private final ObservableSet<Person> unmodifiableStudents = FXCollections.unmodifiableObservableSet(students);
    private final Map<StudentNumber, Set<LocalDate>> attendance = new HashMap<>();
    private final javafx.beans.property.IntegerProperty attendanceVersion =
            new javafx.beans.property.SimpleIntegerProperty(this, "attendanceVersion", 0);

    /**
     * Every field must be present and not null.
     */
    public AcademicClass(ModuleCode moduleCode, ClassName className) {
        this(moduleCode, className, 0);
    }

    /**
     * Creates a class with a configured total number of lessons.
     * A value of zero is retained for classes created by older data files.
     */
    public AcademicClass(ModuleCode moduleCode, ClassName className, int totalLessons) {
        requireAllNonNull(moduleCode, className);
        if (totalLessons < 0) {
            throw new IllegalArgumentException("Total lessons cannot be negative.");
        }
        this.moduleCode = moduleCode;
        this.className = className;
        this.totalLessons = totalLessons;
    }

    public ModuleCode getModuleCode() {
        return moduleCode;
    }

    public ClassName getClassName() {
        return className;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    /**
     * Returns an unmodifiable observable student set, which throws {@code UnsupportedOperationException}
     * if modification is attempted. Membership changes are reflected in this view.
     */
    public ObservableSet<Person> getStudents() {
        return unmodifiableStudents;
    }

    /**
     * Assigns the given student to this class.
     * Returns true if membership was added, or false if the student was already assigned.
     * @throws NullPointerException if {@code person} is null.
     */
    public boolean assignStudent(Person person) {
        requireNonNull(person);
        return students.add(person);
    }

    /**
     * Returns true if both classes have the same module code and class name.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AcademicClass otherAcademicClass)) {
            return false;
        }

        return moduleCode.equals(otherAcademicClass.moduleCode)
                && className.equals(otherAcademicClass.className);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(moduleCode, className);
    }

    @Override
    public String toString() {
        return moduleCode + "-" + className;
    }

    /**
     * Unassigns the specified person from this academic class.
     *
     * @param person The person to unassign from the class.
     * @return true if the person was successfully removed, false if the person
     *         was not assigned to the class.
     */
    public boolean unassignStudent(Person person) {

        if (students.contains(person)) {
            students.remove(person);
            attendance.remove(person.getStudentNumber());
            attendanceVersion.set(attendanceVersion.get() + 1);

            return true; // can output if removal success or not
        }

        return false;
    }

    public boolean hasIdentity(ModuleCode moduleCode, ClassName className) {
        return this.className.equals(className) && this.moduleCode.equals(moduleCode);
    }

    /**
     * Records a date on which a student attended this class.
     * Returns false when the date was already recorded.
     */
    public boolean addAttendance(Person person, LocalDate date) {
        requireAllNonNull(person, date);
        if (!students.contains(person)) {
            return false;
        }
        boolean added = attendance.computeIfAbsent(person.getStudentNumber(), unused -> new HashSet<>()).add(date);
        if (added) {
            attendanceVersion.set(attendanceVersion.get() + 1);
        }
        return added;
    }

    /**
     * Removes a previously recorded attendance date.
     * Returns false when the date was not recorded.
     */
    public boolean removeAttendance(Person person, LocalDate date) {
        requireAllNonNull(person, date);
        Set<LocalDate> dates = attendance.get(person.getStudentNumber());
        if (dates == null || !dates.remove(date)) {
            return false;
        }
        if (dates.isEmpty()) {
            attendance.remove(person.getStudentNumber());
        }
        attendanceVersion.set(attendanceVersion.get() + 1);
        return true;
    }

    /** Returns the immutable attendance dates for the specified student. */
    public Set<LocalDate> getAttendanceDates(Person person) {
        requireNonNull(person);
        return Collections.unmodifiableSet(attendance.getOrDefault(person.getStudentNumber(), Set.of()));
    }

    public int getAttendanceCount(Person person) {
        return getAttendanceDates(person).size();
    }

    /** Returns a property that changes whenever attendance is added or removed. */
    public javafx.beans.property.ReadOnlyIntegerProperty attendanceVersionProperty() {
        return attendanceVersion;
    }

    /** Returns a defensive copy for persistence. */
    public Map<StudentNumber, Set<LocalDate>> getAttendance() {
        Map<StudentNumber, Set<LocalDate>> copy = new HashMap<>();
        attendance.forEach((studentNumber, dates) -> copy.put(studentNumber, Set.copyOf(dates)));
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public void onStudentUpdate(Person oldStudent, Person newStudent) {
        if (students.contains(oldStudent)) {
            students.remove(oldStudent);

            if (newStudent != null) {
                students.add(newStudent);
            }
        }

    }

    @Override
    public void onStudentListReplaced(List<Person> students) {
        requireNonNull(students);

        Set<StudentNumber> memberNumbers = this.students.stream()
                .map(Person::getStudentNumber)
                .collect(Collectors.toSet());

        Set<Person> replacements = new HashSet<>();
        for (Person student : students) {
            requireNonNull(student);
            if (memberNumbers.contains(student.getStudentNumber())) {
                replacements.add(student);
            }
        }

        this.students.clear();
        this.students.addAll(replacements);
    }
}
