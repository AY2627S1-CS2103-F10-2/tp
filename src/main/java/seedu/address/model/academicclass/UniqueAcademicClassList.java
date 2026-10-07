package seedu.address.model.academicclass;

import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A list of academic classes that enforces uniqueness by module code and class name.
 */
public class UniqueAcademicClassList implements Iterable<AcademicClass> {

    private final ObservableList<AcademicClass> internalList = FXCollections.observableArrayList();
    private final ObservableList<AcademicClass> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

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
