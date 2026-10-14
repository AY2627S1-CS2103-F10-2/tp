package seedu.address.model.person;

import java.util.List;

/**
 * Observes changes to student records.
 */
public interface StudentObserver {

    /**
     * Handles the replacement or deletion of a student record.
     *
     * @param oldStudent the original student, which must not be null
     * @param newStudent the replacement student, or null if the original student was deleted
     */
    void onStudentUpdate(Person oldStudent, Person newStudent);

    /**
     * Reconciles existing memberships with the complete replacement student list.
     * Retains memberships by student number, using the replacement person instances,
     * and removes memberships whose student numbers are absent from the list.
     * Does not assign students who were not already members.
     *
     * @param students the complete replacement student list; neither the list nor
     *                 its elements may be null
     * @throws NullPointerException if the list or any of its elements is null
     */
    void onStudentListReplaced(List<Person> students);
}
