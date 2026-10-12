package seedu.address.model.academicclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import seedu.address.model.academicclass.exceptions.DuplicateAcademicClassException;

public class UniqueAcademicClassListTest {

    private final UniqueAcademicClassList uniqueAcademicClassList = new UniqueAcademicClassList();

    @Test
    public void setAcademicClasses_nullListOrElement_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueAcademicClassList.setAcademicClasses(null));
        assertThrows(NullPointerException.class, ()
            -> uniqueAcademicClassList.setAcademicClasses(Arrays.asList((AcademicClass) null)));
    }

    @Test
    public void setAcademicClasses_validList_replacesContentsAndUpdatesView() {
        AcademicClass first = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        AcademicClass second = new AcademicClass(new ModuleCode("ST2334"), new ClassName("T24"));
        var view = uniqueAcademicClassList.asUnmodifiableObservableList();
        uniqueAcademicClassList.setAcademicClasses(List.of(first));
        uniqueAcademicClassList.setAcademicClasses(List.of(second));
        assertEquals(List.of(second), view);
        assertEquals(second, uniqueAcademicClassList.iterator().next());
        uniqueAcademicClassList.setAcademicClasses(view);
        assertEquals(List.of(second), view);
        uniqueAcademicClassList.setAcademicClasses(List.of());
        assertTrue(view.isEmpty());
    }

    @Test
    public void setAcademicClasses_duplicateClasses_throwsAndPreservesContents() {
        AcademicClass first = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        AcademicClass duplicate = new AcademicClass(new ModuleCode("cs2103t"), new ClassName("f10-2"));
        uniqueAcademicClassList.setAcademicClasses(List.of(first));
        assertThrows(DuplicateAcademicClassException.class, ()
            -> uniqueAcademicClassList.setAcademicClasses(List.of(first, duplicate)));
        assertEquals(List.of(first), uniqueAcademicClassList.asUnmodifiableObservableList());
    }

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
