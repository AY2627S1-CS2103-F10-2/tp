package seedu.address.model.academicclass;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.List;
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

    // Data fields
    private final ObservableSet<Person> students = FXCollections.observableSet(new HashSet<>());
    // Cached read-only view of students; reflects membership changes and forwards change notifications.
    private final ObservableSet<Person> unmodifiableStudents = FXCollections.unmodifiableObservableSet(students);

    /**
     * Every field must be present and not null.
     */
    public AcademicClass(ModuleCode moduleCode, ClassName className) {
        requireAllNonNull(moduleCode, className);
        this.moduleCode = moduleCode;
        this.className = className;
    }

    /**
     * Creates a class with the given identity and an independent copy of {@code toCopy}'s memberships.
     * Immutable person records are shared between the two membership sets.
     *
     * @throws NullPointerException if any argument is null
     */
    public AcademicClass(ModuleCode moduleCode, ClassName className, AcademicClass toCopy) {
        this(moduleCode, className);
        this.students.addAll(toCopy.getStudents());
    }


    /** Returns a class with a copy of this class's identity and an independent copy of its memberships. */
    public AcademicClass copy() {
        return new AcademicClass(new ModuleCode(moduleCode.value), new ClassName(className.value), this);
    }

    public ModuleCode getModuleCode() {
        return moduleCode;
    }

    public ClassName getClassName() {
        return className;
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

            return true; // can output if removal success or not
        }

        return false;
    }

    public boolean hasIdentity(ModuleCode moduleCode, ClassName className) {
        return this.className.equals(className) && this.moduleCode.equals(moduleCode);
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

    /**
     * Returns true if the other class has the same module code and class name.
     */
    public boolean isSameClass(AcademicClass otherClass) {
        if (otherClass == this) {
            return true;
        }

        return otherClass != null
                && otherClass.hasIdentity(this.moduleCode, this.className);
    }

}
