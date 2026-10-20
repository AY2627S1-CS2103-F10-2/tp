package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

/**
 * Tests membership changes through parsed commands against the displayed student view.
 */
public class ClassFilterIntegrationTest {
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();

    @BeforeEach
    public void setUp() throws Exception {
        model.addPerson(DANIEL);
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        execute("addclass m/CS2103 c/F10-2");
        execute("assign sn/" + DANIEL.getStudentNumber() + " m/CS2103 c/F10-2");
        execute("assign sn/" + BENSON.getStudentNumber() + " m/CS2103 c/F10-2");
        execute("listbyclass m/CS2103 c/F10-2");
    }

    @Test
    public void execute_unassignFromActiveClass_refreshesExistingView() throws Exception {
        var displayed = model.getFilteredPersonList();
        execute("unassign sn/" + BENSON.getStudentNumber() + " m/CS2103 c/F10-2");
        assertEquals(List.of(DANIEL), displayed);
        execute("unassign sn/" + DANIEL.getStudentNumber() + " m/CS2103 c/F10-2");
        assertEquals(List.of(), displayed);
    }

    @Test
    public void execute_assignToActiveClass_preservesAlphabeticalOrder() throws Exception {
        execute("assign sn/" + ALICE.getStudentNumber() + " m/CS2103 c/F10-2");
        assertEquals(List.of(ALICE, BENSON, DANIEL), model.getFilteredPersonList());
        assertEquals(List.of(DANIEL, BENSON, ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_membershipChangesAfterFind_preservesSearchAndInsertionOrder() throws Exception {
        execute("find Meier");
        execute("unassign sn/" + BENSON.getStudentNumber() + " m/CS2103 c/F10-2");
        execute("assign sn/" + ALICE.getStudentNumber() + " m/CS2103 c/F10-2");
        assertEquals(List.of(DANIEL, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_membershipChangesAfterList_preservesAllStudents() throws Exception {
        execute("list");
        execute("unassign sn/" + BENSON.getStudentNumber() + " m/CS2103 c/F10-2");
        assertEquals(List.of(DANIEL, BENSON, ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_changeOtherClass_preservesActiveClassFilter() throws Exception {
        execute("addclass m/CS2103 c/F11-2");
        execute("assign sn/" + ALICE.getStudentNumber() + " m/CS2103 c/F11-2");
        execute("unassign sn/" + ALICE.getStudentNumber() + " m/CS2103 c/F11-2");
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_failedMembershipChanges_preservesActiveView() {
        assertThrows(CommandException.class, () -> execute("assign sn/" + BENSON.getStudentNumber()
                + " m/CS2103 c/F10-2"));
        assertThrows(CommandException.class, () -> execute("unassign sn/" + ALICE.getStudentNumber()
                + " m/CS2103 c/F10-2"));
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteActiveClass_clearsExistingViewWithoutDeletingStudents() throws Exception {
        var displayed = model.getFilteredPersonList();
        execute("deleteclass m/CS2103 c/F10-2");
        assertEquals(List.of(), displayed);
        assertEquals(List.of(DANIEL, BENSON, ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_deleteOtherClass_preservesActiveClassFilter() throws Exception {
        execute("addclass m/CS2100 c/F10-2");
        execute("assign sn/" + ALICE.getStudentNumber() + " m/CS2100 c/F10-2");
        execute("deleteclass m/CS2100 c/F10-2");
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_recreateDeletedClass_usesNewMembership() throws Exception {
        var displayed = model.getFilteredPersonList();
        execute("deleteclass m/CS2103 c/F10-2");
        execute("addclass m/CS2103 c/F10-2");
        execute("assign sn/" + ALICE.getStudentNumber() + " m/CS2103 c/F10-2");
        assertEquals(List.of(ALICE), displayed);
        execute("unassign sn/" + ALICE.getStudentNumber() + " m/CS2103 c/F10-2");
        assertEquals(List.of(), displayed);
    }

    @Test
    public void execute_deleteClassAfterFind_preservesSearchAndInsertionOrder() throws Exception {
        execute("find Meier");
        execute("deleteclass m/CS2103 c/F10-2");
        assertEquals(List.of(DANIEL, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteClassAfterList_preservesAllStudents() throws Exception {
        execute("list");
        execute("deleteclass m/CS2103 c/F10-2");
        assertEquals(List.of(DANIEL, BENSON, ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteMissingClass_preservesActiveView() {
        assertThrows(CommandException.class, () -> execute("deleteclass m/CS2103 c/F11-2"));
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    private void execute(String command) throws ParseException, CommandException {
        parser.parseCommand(command).execute(model);
    }
}
