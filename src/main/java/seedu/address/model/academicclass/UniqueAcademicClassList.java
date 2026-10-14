package seedu.address.model.academicclass;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.academicclass.exceptions.AcademicClassNotFoundException;
import seedu.address.model.academicclass.exceptions.DuplicateAcademicClassException;

/**
 * A list of academic classes that enforces uniqueness by module code and class name.
 */
public class UniqueAcademicClassList implements Iterable<AcademicClass> {

    private final ObservableList<AcademicClass> internalList = FXCollections.observableArrayList();
    private final ObservableList<AcademicClass> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Replaces the contents with {@code academicClasses}, which must not contain nulls or duplicates.
     */
    public void setAcademicClasses(List<AcademicClass> academicClasses) {
        requireAllNonNull(academicClasses);
        if (new HashSet<>(academicClasses).size() != academicClasses.size()) {
            throw new DuplicateAcademicClassException();
        }
        internalList.setAll(List.copyOf(academicClasses));
    }

    /**
     * Removes the equivalent academic class from the list.
     * The academic class must exist in the list.
     */
    public void remove(AcademicClass toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new AcademicClassNotFoundException();
        }
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<AcademicClass> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<AcademicClass> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniqueAcademicClassList otherUniqueAcademicClassList)) {
            return false;
        }

        return internalList.equals(otherUniqueAcademicClassList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }
}
