package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests alphabetical display ordering without reordering stored person records.
 */
public class ModelManagerSortedPersonListTest {

    private final AddressBook addressBook = new AddressBookBuilder()
            .withPerson(BENSON).withPerson(ALICE).withPerson(DANIEL).build();
    private final ModelManager model = new ModelManager(addressBook, new UserPrefs());

    @Test
    public void updateFilteredPersonListAlphabetically_matchingPersons_sortsOnlyMatches() {
        model.updateFilteredPersonListAlphabetically(person -> person.isSamePerson(ALICE)
                || person.isSamePerson(BENSON));

        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getAddressBook().getPersonList());
    }

    @Test
    public void updateFilteredPersonListAlphabetically_noMatches_displaysEmptyList() {
        model.updateFilteredPersonListAlphabetically(person -> false);

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(addressBook, model.getAddressBook());
    }

    @Test
    public void updateFilteredPersonListAlphabetically_sameNames_keepsDistinctStudents() {
        Person anotherAlice = new PersonBuilder(ALICE).withStudentNumber("A1111111B")
                .withEmail("anotheralice@example.com").withTelegram(null).build();
        model.addPerson(anotherAlice);

        model.updateFilteredPersonListAlphabetically(person -> person.getName().equals(ALICE.getName()));

        assertEquals(List.of(ALICE, anotherAlice), model.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonListAlphabetically_nullPredicate_preservesView() {
        model.updateFilteredPersonListAlphabetically(PREDICATE_SHOW_ALL_PERSONS);

        assertThrows(NullPointerException.class, () -> model.updateFilteredPersonListAlphabetically(null));
        assertEquals(List.of(ALICE, BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_sortedView_isUnmodifiable() {
        model.updateFilteredPersonListAlphabetically(PREDICATE_SHOW_ALL_PERSONS);

        assertThrows(UnsupportedOperationException.class, () -> model.getFilteredPersonList().remove(0));
        assertEquals(addressBook, model.getAddressBook());
    }

    @Test
    public void setPerson_nameChanges_updatesExistingSortedView() {
        var displayedPersons = model.getFilteredPersonList();
        model.updateFilteredPersonListAlphabetically(PREDICATE_SHOW_ALL_PERSONS);
        Person renamedAlice = new PersonBuilder(ALICE).withName("Zoe Pauline").build();

        model.setPerson(ALICE, renamedAlice);

        assertSame(displayedPersons, model.getFilteredPersonList());
        assertEquals(List.of(BENSON, DANIEL, renamedAlice), displayedPersons);
        assertEquals(List.of(BENSON, renamedAlice, DANIEL), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_deleteFromSortedView_deletesDisplayedPerson() throws CommandException {
        model.updateFilteredPersonListAlphabetically(PREDICATE_SHOW_ALL_PERSONS);

        new DeleteCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(BENSON, DANIEL), model.getAddressBook().getPersonList());
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_editFromSortedView_editsDisplayedPerson() throws CommandException {
        model.updateFilteredPersonListAlphabetically(PREDICATE_SHOW_ALL_PERSONS);
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setName(new Name("Zoe Pauline"));
        Person renamedAlice = new PersonBuilder(ALICE).withName("Zoe Pauline").build();

        new EditCommand(Index.fromOneBased(1), descriptor).execute(model);

        assertEquals(List.of(BENSON, renamedAlice, DANIEL), model.getAddressBook().getPersonList());
        assertEquals(List.of(BENSON, DANIEL, renamedAlice), model.getFilteredPersonList());
    }

    @Test
    public void execute_listAfterSortedFilter_restoresAllPersonsInInsertionOrder() {
        model.updateFilteredPersonListAlphabetically(person -> person.isSamePerson(ALICE));

        new ListCommand().execute(model);

        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_findAfterSortedFilter_restoresMatchingPersonsInInsertionOrder() {
        AddressBook reversedAddressBook = new AddressBookBuilder()
                .withPerson(DANIEL).withPerson(BENSON).withPerson(ALICE).build();
        ModelManager reversedModel = new ModelManager(reversedAddressBook, new UserPrefs());
        reversedModel.updateFilteredPersonListAlphabetically(PREDICATE_SHOW_ALL_PERSONS);

        new FindCommand(new NameContainsKeywordsPredicate(List.of("Meier"))).execute(reversedModel);

        assertEquals(List.of(DANIEL, BENSON), reversedModel.getFilteredPersonList());
    }

    @Test
    public void addPerson_afterSortedFilter_preservesMatchingPersons() {
        model.updateFilteredPersonListAlphabetically(person -> person.isSamePerson(ALICE));
        Person anotherAlice = new PersonBuilder(ALICE).withStudentNumber("A1111111B")
                .withEmail("anotheralice@example.com").withTelegram(null).build();

        model.addPerson(anotherAlice);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ALICE, DANIEL, anotherAlice), model.getAddressBook().getPersonList());
    }

    @Test
    public void equals_differentDisplayOrder_returnsFalse() {
        ModelManager insertionOrderModel = new ModelManager(addressBook, new UserPrefs());
        model.updateFilteredPersonListAlphabetically(PREDICATE_SHOW_ALL_PERSONS);

        assertFalse(model.equals(insertionOrderModel));
    }
}
