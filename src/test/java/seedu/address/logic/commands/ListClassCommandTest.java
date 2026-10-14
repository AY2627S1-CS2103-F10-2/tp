package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.testutil.TypicalPersons;

/** Contains tests for {@link ListClassCommand}. */
public class ListClassCommandTest {

    @Test
    public void execute_resetsClassFilterAndPreservesInsertionOrder() {
        AcademicClass first = new AcademicClass(new ModuleCode("CS2100"), new ClassName("L03"));
        AcademicClass second = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("T01"));
        AddressBook addressBook = new AddressBook();
        addressBook.setAcademicClasses(List.of(first, second));
        ModelManager model = new ModelManager(addressBook, new UserPrefs());

        model.updateFilteredAcademicClassList(academicClass -> academicClass.equals(second));
        CommandResult result = new ListClassCommand().execute(model);

        assertEquals(ListClassCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(first, second), model.getFilteredAcademicClassList());
        assertEquals(List.of(first, second), model.getAcademicClassList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListClassCommand().execute(null));
    }

    @Test
    public void execute_thenFindStudent_keepsBothFilteredViewsIndependent() throws Exception {
        AcademicClass first = new AcademicClass(new ModuleCode("CS2100"), new ClassName("L03"));
        AcademicClass second = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("T01"));
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(TypicalPersons.ALICE);
        addressBook.setAcademicClasses(List.of(first, second));
        ModelManager model = new ModelManager(addressBook, new UserPrefs());

        model.updateFilteredAcademicClassList(academicClass -> academicClass.equals(second));
        new ListClassCommand().execute(model);
        new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice"))).execute(model);

        assertEquals(List.of(first, second), model.getFilteredAcademicClassList());
        assertEquals(List.of(TypicalPersons.ALICE), model.getFilteredPersonList());
    }
}
