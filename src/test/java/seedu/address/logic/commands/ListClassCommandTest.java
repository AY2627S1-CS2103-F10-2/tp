package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/** Contains tests for {@link ListClassCommand}. */
public class ListClassCommandTest {

    @Test
    public void execute_returnsSuccessAndPreservesInsertionOrder() {
        AcademicClass first = new AcademicClass(new ModuleCode("CS2100"), new ClassName("L03"));
        AcademicClass second = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("T01"));
        AddressBook addressBook = new AddressBook();
        addressBook.setAcademicClasses(List.of(first, second));
        Model model = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new ListClassCommand().execute(model);

        assertEquals(ListClassCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(first, second), model.getAddressBook().getAcademicClassList());
    }

}
