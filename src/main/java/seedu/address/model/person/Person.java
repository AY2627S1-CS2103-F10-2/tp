package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null (except the optional Telegram), field values are validated,
 * immutable.
 */
public class Person {

    // Identity fields
    private final StudentNumber studentNumber;
    private final Name name;
    private final Email email;

    // Data fields
    private final Telegram telegram; // optional, may be null
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null, except {@code telegram} which is optional.
     */
    public Person(StudentNumber studentNumber, Name name, Email email, Telegram telegram, Set<Tag> tags) {
        requireAllNonNull(studentNumber, name, email, tags);
        this.studentNumber = studentNumber;
        this.name = name;
        this.email = email;
        this.telegram = telegram;
        this.tags.addAll(tags);
    }

    public StudentNumber getStudentNumber() {
        return studentNumber;
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Optional<Telegram> getTelegram() {
        return Optional.ofNullable(telegram);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same student number.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getStudentNumber().equals(getStudentNumber());
    }

    /**
     * Returns true if both persons have the same email, ignoring case.
     */
    public boolean hasSameEmail(Person otherPerson) {
        return otherPerson != null
                && otherPerson.getEmail().value.equalsIgnoreCase(getEmail().value);
    }

    /**
     * Returns true if both persons have a Telegram username and the usernames are the same, ignoring case.
     */
    public boolean hasSameTelegram(Person otherPerson) {
        return otherPerson != null
                && telegram != null
                && telegram.equals(otherPerson.telegram);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return studentNumber.equals(otherPerson.studentNumber)
                && name.equals(otherPerson.name)
                && email.equals(otherPerson.email)
                && Objects.equals(telegram, otherPerson.telegram)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(studentNumber, name, email, telegram, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentNumber", studentNumber)
                .add("name", name)
                .add("email", email)
                .add("telegram", telegram)
                .add("tags", tags)
                .toString();
    }

}
