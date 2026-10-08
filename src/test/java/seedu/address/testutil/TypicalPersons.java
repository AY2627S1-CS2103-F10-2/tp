package seedu.address.testutil;

import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STUDENT_NUMBER_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STUDENT_NUMBER_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;

/**
 * A utility class containing a list of {@code Person} objects to be used in tests.
 * Every person has a distinct student number and email, and Telegram usernames are distinct where present.
 */
public class TypicalPersons {

    public static final Person ALICE = new PersonBuilder().withStudentNumber("A0123456X").withName("Alice Pauline")
            .withEmail("alice@example.com").withTelegram("alice_pauline").withTags("friends").build();
    public static final Person BENSON = new PersonBuilder().withStudentNumber("A0234567Y").withName("Benson Meier")
            .withEmail("johnd@example.com").withTelegram("benson_meier").withTags("owesMoney", "friends").build();
    public static final Person CARL = new PersonBuilder().withStudentNumber("A0345678Z").withName("Carl Kurz")
            .withEmail("heinz@example.com").build();
    public static final Person DANIEL = new PersonBuilder().withStudentNumber("A0456789A").withName("Daniel Meier")
            .withEmail("cornelia@example.com").withTags("friends").build();
    public static final Person ELLE = new PersonBuilder().withStudentNumber("A0567890B").withName("Elle Meyer")
            .withEmail("werner@example.com").withTelegram("elle_meyer").build();
    public static final Person FIONA = new PersonBuilder().withStudentNumber("A0678901C").withName("Fiona Kunz")
            .withEmail("lydia@example.com").build();
    public static final Person GEORGE = new PersonBuilder().withStudentNumber("A0789012D").withName("George Best")
            .withEmail("anna@example.com").build();

    // Manually added
    public static final Person HOON = new PersonBuilder().withStudentNumber("A0890123E").withName("Hoon Meier")
            .withEmail("stefan@example.com").build();
    public static final Person IDA = new PersonBuilder().withStudentNumber("A0901234F").withName("Ida Mueller")
            .withEmail("hans@example.com").build();

    // Manually added - Person's details found in {@code CommandTestUtil}
    public static final Person AMY = new PersonBuilder().withStudentNumber(VALID_STUDENT_NUMBER_AMY)
            .withName(VALID_NAME_AMY).withEmail(VALID_EMAIL_AMY).withTelegram(VALID_TELEGRAM_AMY)
            .withTags(VALID_TAG_FRIEND).build();
    public static final Person BOB = new PersonBuilder().withStudentNumber(VALID_STUDENT_NUMBER_BOB)
            .withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB).withTelegram(VALID_TELEGRAM_BOB)
            .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();

    public static final String KEYWORD_MATCHING_MEIER = "Meier"; // A keyword that matches MEIER

    private TypicalPersons() {} // prevents instantiation

    /**
     * Returns an {@code AddressBook} with all the typical persons.
     */
    public static AddressBook getTypicalAddressBook() {
        AddressBook ab = new AddressBook();
        for (Person person : getTypicalPersons()) {
            ab.addPerson(person);
        }
        return ab;
    }

    public static List<Person> getTypicalPersons() {
        return new ArrayList<>(Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }
}
