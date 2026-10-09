package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.UniqueAcademicClassList;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicate persons and academic classes are not allowed.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueAcademicClassList academicClasses = new UniqueAcademicClassList();

    /**
     * Creates an empty AddressBook.
     */
    public AddressBook() {}

    /**
     * Creates an AddressBook using the persons and academic classes in {@code toBeCopied}.
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Replaces the contents of the academic class list.
     * {@code academicClasses} must not contain nulls or duplicate classes.
     */
    public void setAcademicClasses(List<AcademicClass> academicClasses) {
        this.academicClasses.setAcademicClasses(academicClasses);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     * Both lists are validated before either existing list is modified.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        UniquePersonList replacementPersons = new UniquePersonList();
        replacementPersons.setPersons(newData.getPersonList());
        UniqueAcademicClassList replacementClasses = new UniqueAcademicClassList();
        replacementClasses.setAcademicClasses(newData.getAcademicClassList());

        persons.setPersons(replacementPersons);
        academicClasses.setAcademicClasses(replacementClasses.asUnmodifiableObservableList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Returns true if a person with the same email (ignoring case) as {@code person} exists in the address book.
     */
    public boolean hasEmail(Person person) {
        requireNonNull(person);
        return persons.containsEmail(person);
    }

    /**
     * Returns true if a person with the same Telegram username (ignoring case) as {@code person} exists in the
     * address book.
     */
    public boolean hasTelegram(Person person) {
        requireNonNull(person);
        return persons.containsTelegram(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// academic class-level operations

    /**
     * Returns true if an academic class with the same module code and class name exists in the address book.
     */
    public boolean hasAcademicClass(AcademicClass academicClass) {
        requireNonNull(academicClass);
        return academicClasses.asUnmodifiableObservableList().contains(academicClass);
    }

    /**
     * Adds an academic class to the address book.
     * The academic class must not already exist in the address book.
     */
    public void addAcademicClass(AcademicClass academicClass) {
        requireNonNull(academicClass);
        List<AcademicClass> updatedAcademicClasses = new ArrayList<>(academicClasses.asUnmodifiableObservableList());
        updatedAcademicClasses.add(academicClass);
        academicClasses.setAcademicClasses(updatedAcademicClasses);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("academicClasses", academicClasses)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<AcademicClass> getAcademicClassList() {
        return academicClasses.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && academicClasses.equals(otherAddressBook.academicClasses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, academicClasses);
    }
}
