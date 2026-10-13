package seedu.address.model.academicclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests academic class membership updates in response to student notifications.
 */
public class AcademicClassStudentObserverTest {
    private final AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("OB1"));

    @Test
    public void onStudentListReplaced_updatedDetails_usesReplacementReferenceWithoutEnrollingOthers() {
        academicClass.assignStudent(ALICE);
        Person updatedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        academicClass.onStudentListReplaced(List.of(updatedAlice, BENSON));

        assertEquals(Set.of(updatedAlice), academicClass.getStudents());
        assertSame(updatedAlice, academicClass.getStudents().iterator().next());
    }

    @Test
    public void onStudentListReplaced_missingStudents_removesOnlyMissingMemberships() {
        academicClass.assignStudent(ALICE);
        academicClass.assignStudent(BENSON);

        academicClass.onStudentListReplaced(List.of(BENSON));

        assertEquals(Set.of(BENSON), academicClass.getStudents());
    }

    @Test
    public void onStudentListReplaced_emptyList_clearsMemberships() {
        academicClass.assignStudent(ALICE);

        academicClass.onStudentListReplaced(List.of());

        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void onStudentListReplaced_newStudentNumber_doesNotInferExistingMembership() {
        academicClass.assignStudent(ALICE);
        Person replacement = new PersonBuilder(ALICE).withStudentNumber("A8888883K").build();

        academicClass.onStudentListReplaced(List.of(replacement));

        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void onStudentListReplaced_invalidList_preservesMemberships() {
        academicClass.assignStudent(ALICE);
        Person updatedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        assertThrows(NullPointerException.class, () -> academicClass.onStudentListReplaced(null));
        assertThrows(NullPointerException.class, () ->
                academicClass.onStudentListReplaced(Arrays.asList(updatedAlice, null)));

        assertSame(ALICE, academicClass.getStudents().iterator().next());
    }

    @Test
    public void onStudentListReplaced_preservesReadOnlyViewAndNotifiesItsListeners() {
        academicClass.assignStudent(ALICE);
        ObservableSet<Person> view = academicClass.getStudents();
        List<Person> removed = new ArrayList<>();
        List<Person> added = new ArrayList<>();
        view.addListener((SetChangeListener<Person>) change -> {
            if (change.wasRemoved()) {
                removed.add(change.getElementRemoved());
            }
            if (change.wasAdded()) {
                added.add(change.getElementAdded());
            }
        });
        Person updatedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        academicClass.onStudentListReplaced(List.of(updatedAlice));

        assertSame(view, academicClass.getStudents());
        assertSame(updatedAlice, view.iterator().next());
        assertEquals(List.of(ALICE), removed);
        assertEquals(List.of(updatedAlice), added);
        assertThrows(UnsupportedOperationException.class, () -> view.add(BENSON));
    }

    @Test
    public void onStudentUpdate_assignedStudent_replacesStoredReference() {
        academicClass.assignStudent(ALICE);
        academicClass.assignStudent(BENSON);
        Person editedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        academicClass.onStudentUpdate(ALICE, editedAlice);

        assertEquals(Set.of(editedAlice, BENSON), academicClass.getStudents());
        assertTrue(academicClass.unassignStudent(editedAlice));
        assertEquals(Set.of(BENSON), academicClass.getStudents());
    }

    @Test
    public void onStudentUpdate_studentNotAssigned_doesNotAddMembership() {
        academicClass.assignStudent(BENSON);
        Person editedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();

        academicClass.onStudentUpdate(ALICE, editedAlice);

        assertEquals(Set.of(BENSON), academicClass.getStudents());
    }

    @Test
    public void onStudentUpdate_nullReplacement_removesOnlyDeletedStudent() {
        academicClass.assignStudent(ALICE);
        academicClass.assignStudent(BENSON);

        academicClass.onStudentUpdate(ALICE, null);

        assertEquals(Set.of(BENSON), academicClass.getStudents());
    }

    @Test
    public void onStudentUpdate_equalReplacement_storesNewInstance() {
        academicClass.assignStudent(ALICE);
        Person replacement = new PersonBuilder(ALICE).build();

        academicClass.onStudentUpdate(ALICE, replacement);

        assertSame(replacement, academicClass.getStudents().iterator().next());
        assertEquals(1, academicClass.getStudents().size());
    }

    @Test
    public void onStudentUpdate_membershipChanges_notifyReadOnlyViewListeners() {
        academicClass.assignStudent(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withName("Updated Alice").build();
        List<Person> removed = new ArrayList<>();
        List<Person> added = new ArrayList<>();
        academicClass.getStudents().addListener((SetChangeListener<Person>) change -> {
            if (change.wasRemoved()) {
                removed.add(change.getElementRemoved());
            }
            if (change.wasAdded()) {
                added.add(change.getElementAdded());
            }
        });

        academicClass.onStudentUpdate(ALICE, editedAlice);
        academicClass.onStudentUpdate(editedAlice, null);

        assertEquals(List.of(ALICE, editedAlice), removed);
        assertEquals(List.of(editedAlice), added);
        assertTrue(academicClass.getStudents().isEmpty());
    }
}
