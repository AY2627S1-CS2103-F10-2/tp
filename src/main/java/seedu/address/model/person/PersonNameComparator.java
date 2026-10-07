package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;

/**
 * Compares persons alphabetically by name, ignoring case before comparing original spelling.
 * Persons with identical names compare equally, regardless of their other details.
 * This ordering is not consistent with {@link Person#equals(Object)} and must not be used
 * to determine uniqueness in a sorted set or map.
 */
public class PersonNameComparator implements Comparator<Person> {

    /**
     * Compares the names of two persons without modifying either person.
     *
     * @throws NullPointerException if either person is null.
     */
    @Override
    public int compare(Person firstPerson, Person secondPerson) {
        requireAllNonNull(firstPerson, secondPerson);
        String firstName = firstPerson.getName().fullName;
        String secondName = secondPerson.getName().fullName;
        int comparison = firstName.compareToIgnoreCase(secondName);

        return comparison != 0 ? comparison : firstName.compareTo(secondName);
    }
}
