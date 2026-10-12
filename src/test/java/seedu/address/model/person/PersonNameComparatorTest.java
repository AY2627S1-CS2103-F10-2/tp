package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonNameComparatorTest {

    private final PersonNameComparator comparator = new PersonNameComparator();

    @Test
    public void compare_differentNames_returnsAlphabeticalOrder() {
        Person alice = new PersonBuilder().withName("Alice Tan").build();
        Person benjamin = new PersonBuilder().withName("Benjamin Lim").build();

        assertTrue(comparator.compare(alice, benjamin) < 0);
        assertTrue(comparator.compare(benjamin, alice) > 0);
    }

    @Test
    public void compare_mixedCaseNames_ignoresCaseForPrimaryOrder() {
        Person alice = new PersonBuilder().withName("alice Tan").build();
        Person benjamin = new PersonBuilder().withName("Benjamin Lim").build();

        assertTrue(comparator.compare(alice, benjamin) < 0);
        assertTrue(comparator.compare(benjamin, alice) > 0);
    }

    @Test
    public void compare_namesDifferOnlyInCase_usesOriginalSpellingAsTieBreaker() {
        Person uppercaseAlice = new PersonBuilder().withName("Alice Tan").build();
        Person lowercaseAlice = new PersonBuilder().withName("alice Tan").build();

        assertTrue(comparator.compare(uppercaseAlice, lowercaseAlice) < 0);
        assertTrue(comparator.compare(lowercaseAlice, uppercaseAlice) > 0);
    }

    @Test
    public void compare_identicalNamesWithDifferentDetails_returnsZero() {
        Person firstAlice = new PersonBuilder().withName("Alice Tan").withEmail("first@example.com").build();
        Person secondAlice = new PersonBuilder().withName("Alice Tan").withEmail("second@example.com").build();

        assertEquals(0, comparator.compare(firstAlice, secondAlice));
        assertEquals(0, comparator.compare(secondAlice, firstAlice));
    }

    @Test
    public void compare_samePerson_returnsZero() {
        Person alice = new PersonBuilder().withName("Alice Tan").build();

        assertEquals(0, comparator.compare(alice, alice));
    }

    @Test
    public void compare_orderedNames_isTransitive() {
        Person alice = new PersonBuilder().withName("alice Tan").build();
        Person benjamin = new PersonBuilder().withName("Benjamin Lim").build();
        Person chloe = new PersonBuilder().withName("chloe Ng").build();

        assertTrue(comparator.compare(alice, benjamin) < 0);
        assertTrue(comparator.compare(benjamin, chloe) < 0);
        assertTrue(comparator.compare(alice, chloe) < 0);
    }

    @Test
    public void compare_nullPerson_throwsNullPointerException() {
        Person alice = new PersonBuilder().withName("Alice Tan").build();

        assertThrows(NullPointerException.class, () -> comparator.compare(null, alice));
        assertThrows(NullPointerException.class, () -> comparator.compare(alice, null));
        assertThrows(NullPointerException.class, () -> comparator.compare(null, null));
    }

    @Test
    public void sorted_multiplePersons_preservesPersonsWithIdenticalNames() {
        Person firstAlice = new PersonBuilder().withName("Alice Tan").withEmail("first@example.com").build();
        Person secondAlice = new PersonBuilder().withName("Alice Tan").withEmail("second@example.com").build();
        Person benjamin = new PersonBuilder().withName("Benjamin Lim").build();
        List<Person> persons = List.of(benjamin, firstAlice, secondAlice);

        List<Person> sortedPersons = persons.stream().sorted(comparator).toList();

        assertEquals(List.of(firstAlice, secondAlice, benjamin), sortedPersons);
        assertEquals(List.of(benjamin, firstAlice, secondAlice), persons);
    }
}
