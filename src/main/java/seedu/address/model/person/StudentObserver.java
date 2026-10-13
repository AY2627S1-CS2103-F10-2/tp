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

    void onStudentListReplaced(List<Person> persons);
}
