package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {
    @Test
    public void format_withoutTags_omitsTagsLabelAndTrailingSeparator() {
        Person person = new PersonBuilder().withTags().build();
        assertEquals("Amy Bee; Student Number: A0000000Z; Email: amy@gmail.com", Messages.format(person));
    }

    @Test
    public void format_withTelegramWithoutTags_endsWithTelegram() {
        Person person = new PersonBuilder().withTelegram("amy_bee").withTags().build();
        assertEquals("Amy Bee; Student Number: A0000000Z; Email: amy@gmail.com; Telegram: @amy_bee",
                Messages.format(person));
    }

    @Test
    public void format_withTag_includesTagsLabelAndValue() {
        Person person = new PersonBuilder().withTags("tutorial").build();
        assertEquals("Amy Bee; Student Number: A0000000Z; Email: amy@gmail.com; Tags: [tutorial]",
                Messages.format(person));
    }
}
