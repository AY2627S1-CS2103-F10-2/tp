package seedu.address.model.academicclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class UniqueAcademicClassListTest {

    private final UniqueAcademicClassList uniqueAcademicClassList = new UniqueAcademicClassList();

    @Test
    public void asUnmodifiableObservableList_emptyList_returnsEmptyList() {
        assertTrue(uniqueAcademicClassList.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        assertThrows(UnsupportedOperationException.class, ()
            -> uniqueAcademicClassList.asUnmodifiableObservableList().add(academicClass));
        assertThrows(UnsupportedOperationException.class, ()
            -> uniqueAcademicClassList.asUnmodifiableObservableList().remove(0));
        assertTrue(uniqueAcademicClassList.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void iterator_emptyList_hasNoElements() {
        Iterator<AcademicClass> iterator = uniqueAcademicClassList.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void equals() {
        // same object -> returns true
        assertTrue(uniqueAcademicClassList.equals(uniqueAcademicClassList));

        // same values -> returns true
        UniqueAcademicClassList otherUniqueAcademicClassList = new UniqueAcademicClassList();
        assertTrue(uniqueAcademicClassList.equals(otherUniqueAcademicClassList));
        assertTrue(otherUniqueAcademicClassList.equals(uniqueAcademicClassList));

        // null -> returns false
        assertFalse(uniqueAcademicClassList.equals(null));

        // different types -> returns false
        assertFalse(uniqueAcademicClassList.equals(5));
    }

    @Test
    public void hashCode_equalLists_returnsSameHashCode() {
        UniqueAcademicClassList otherUniqueAcademicClassList = new UniqueAcademicClassList();
        assertEquals(uniqueAcademicClassList.hashCode(), otherUniqueAcademicClassList.hashCode());
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniqueAcademicClassList.asUnmodifiableObservableList().toString(),
                uniqueAcademicClassList.toString());
    }
}
