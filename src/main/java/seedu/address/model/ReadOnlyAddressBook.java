package seedu.address.model;

import java.util.Optional;

import javafx.collections.ObservableList;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns an unmodifiable view of the academic class list.
     * This list will not contain any duplicate academic classes.
     */
    ObservableList<AcademicClass> getAcademicClassList();

    /**
     * Returns the academic class with the given module code and class name.
     *
     * @param moduleCode the module code of the academic class to find
     * @param className the class name of the academic class to find
     * @return an {@code Optional} containing the matching academic class, or
     *         {@code Optional#empty()} if no matching class exists
     * @throws NullPointerException if {@code moduleCode} or {@code className} is null
     */
    Optional<AcademicClass> findAcademicClass(ModuleCode moduleCode, ClassName className);

    /**
     * Finds a person by their exact student number in the full address book, regardless of the current filter.
     * Returns an empty Optional if no matching person exists. Does not change the current filter.
     * @throws NullPointerException if {@code studentNumber} is null.
     */
    Optional<Person> findPersonByStudentNumber(StudentNumber studentNumber);

}
