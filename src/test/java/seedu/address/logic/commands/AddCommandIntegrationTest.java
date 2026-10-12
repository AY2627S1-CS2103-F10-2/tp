package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_newPersonWithoutTelegram_success() {
        Person validPerson = new PersonBuilder().withTelegram(null).build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_duplicateStudentNumber_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model, Messages.MESSAGE_DUPLICATE_STUDENT_NUMBER);
    }

    @Test
    public void execute_duplicateEmail_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameEmail = new PersonBuilder().withEmail(personInList.getEmail().value.toUpperCase()).build();
        assertCommandFailure(new AddCommand(sameEmail), model, Messages.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void execute_duplicateTelegram_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameTelegram = new PersonBuilder().withTelegram(personInList.getTelegram().get().value.toUpperCase())
                .build();
        assertCommandFailure(new AddCommand(sameTelegram), model, Messages.MESSAGE_DUPLICATE_TELEGRAM);
    }

}
