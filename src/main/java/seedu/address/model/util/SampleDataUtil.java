package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;
import seedu.address.model.person.Telegram;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new StudentNumber("A0123456X"), new Name("Alex Yeoh"), new Email("alexyeoh@example.com"),
                new Telegram("alex_yeoh"),
                getTagSet("friends")),
            new Person(new StudentNumber("A0234567Y"), new Name("Bernice Yu"), new Email("berniceyu@example.com"),
                new Telegram("bernice_yu"),
                getTagSet("colleagues", "friends")),
            new Person(new StudentNumber("A0345678Z"), new Name("Charlotte Oliveiro"),
                new Email("charlotte@example.com"), null,
                getTagSet("neighbours")),
            new Person(new StudentNumber("A0456789A"), new Name("David Li"), new Email("lidavid@example.com"),
                null,
                getTagSet("family")),
            new Person(new StudentNumber("A0567890B"), new Name("Irfan Ibrahim"), new Email("irfan@example.com"),
                new Telegram("irfan_ibrahim"),
                getTagSet("classmates")),
            new Person(new StudentNumber("A0678901C"), new Name("Roy Balakrishnan"), new Email("royb@example.com"),
                null,
                getTagSet("colleagues"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
