package seedu.address.testutil;

import java.util.HashSet;
import java.util.Set;

import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;
import seedu.address.model.person.Telegram;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_STUDENT_NUMBER = "A0123456X";
    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";

    private StudentNumber studentNumber;
    private Name name;
    private Email email;
    private Telegram telegram;
    private Set<Tag> tags;

    /**
     * Creates a {@code PersonBuilder} with the default details. The default person has no Telegram username.
     */
    public PersonBuilder() {
        studentNumber = new StudentNumber(DEFAULT_STUDENT_NUMBER);
        name = new Name(DEFAULT_NAME);
        email = new Email(DEFAULT_EMAIL);
        telegram = null;
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        studentNumber = personToCopy.getStudentNumber();
        name = personToCopy.getName();
        email = personToCopy.getEmail();
        telegram = personToCopy.getTelegram().orElse(null);
        tags = new HashSet<>(personToCopy.getTags());
    }

    /**
     * Sets the {@code StudentNumber} of the {@code Person} that we are building.
     */
    public PersonBuilder withStudentNumber(String studentNumber) {
        this.studentNumber = new StudentNumber(studentNumber);
        return this;
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the {@code Telegram} of the {@code Person} that we are building.
     * A null {@code telegram} means the person has no Telegram username.
     */
    public PersonBuilder withTelegram(String telegram) {
        this.telegram = telegram == null ? null : new Telegram(telegram);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    public Person build() {
        return new Person(studentNumber, name, email, telegram, tags);
    }

}
