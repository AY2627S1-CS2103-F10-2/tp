package seedu.address.model.academicclass;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Represents a Class in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class AcademicClass {

    // Identity fields
    private final ModuleCode moduleCode;
    private final ClassName className;

    // Data fields
    private final Set<Person> students = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public AcademicClass(ModuleCode moduleCode, ClassName className) {
        requireAllNonNull(moduleCode, className);
        this.moduleCode = moduleCode;
        this.className = className;
    }

    public ModuleCode getModuleCode() {
        return moduleCode;
    }

    public ClassName getClassName() {
        return className;
    }

    /**
     * Returns an immutable student set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Person> getStudents() {
        return Collections.unmodifiableSet(students);
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
        return new ToStringBuilder(this)
                .add("moduleCode", moduleCode)
                .add("className", className)
                .add("students", students)
                .toString();
    }

}
