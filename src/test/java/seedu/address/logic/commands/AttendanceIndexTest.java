package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/** Checks attendance targeting against the displayed list rather than storage order. */
public class AttendanceIndexTest {
    private static final String ARGUMENTS = " m/CS2103 c/F10-2 d/2024-02-29";
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_sortedClassView_targetsDisplayedStudentAndPreservesView() throws Exception {
        ModelManager model = assignedModel();
        model.filterPersonsByClass(model.getAcademicClassList().getFirst());
        parser.parseCommand("addattendance 1" + ARGUMENTS).execute(model);
        assertSame(ALICE, model.getAttendanceList().getFirst().getStudent());
        assertEquals(Set.of(LocalDate.of(2024, 2, 29)),
                model.getAttendanceList().getFirst().getAttendedDates());
        parser.parseCommand("deleteattendance 1" + ARGUMENTS).execute(model);
        assertEquals(Set.of(), model.getAttendanceList().getFirst().getAttendedDates());
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyView_rejectsWithoutChangingAttendance() throws Exception {
        ModelManager model = assignedModel();
        model.updateFilteredPersonList(person -> false);
        for (String command : List.of("addattendance", "deleteattendance")) {
            Command parsed = parser.parseCommand(command + " 1" + ARGUMENTS);
            assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                    parsed.execute(model));
        }
        assertEquals(List.of(), model.getAttendanceList());
    }

    @Test
    public void execute_filteredView_rejectsIndexOnlyValidInFullList() throws Exception {
        ModelManager model = assignedModel();
        model.updateFilteredPersonList(ALICE::equals);
        for (String command : List.of("addattendance", "deleteattendance")) {
            Command parsed = parser.parseCommand(command + " 2" + ARGUMENTS);
            assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                    parsed.execute(model));
        }
        assertEquals(List.of(), model.getAttendanceList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_unassignedStudent_listAllowsDeletingRetainedHistory() throws Exception {
        ModelManager model = assignedModel();
        AcademicClass academicClass = model.getAcademicClassList().getFirst();
        model.filterPersonsByClass(academicClass);
        parser.parseCommand("addattendance 1" + ARGUMENTS).execute(model);
        model.unassignStudentFromClass(ALICE, academicClass);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        // Index 1 now means Benson: it must not silently delete Alice's history.
        Command wrongStudent = parser.parseCommand("deleteattendance 1" + ARGUMENTS);
        assertThrows(CommandException.class, () -> wrongStudent.execute(model));
        assertEquals(Set.of(LocalDate.of(2024, 2, 29)),
                model.getAttendanceList().getFirst().getAttendedDates());
        parser.parseCommand("list").execute(model);
        assertEquals(List.of(BENSON, ALICE), model.getFilteredPersonList());
        parser.parseCommand("deleteattendance 2" + ARGUMENTS).execute(model);
        assertEquals(Set.of(), model.getAttendanceList().getFirst().getAttendedDates());
        assertEquals(Set.of(BENSON), academicClass.getStudents());
    }

    @Test
    public void parse_invalidIndexOrOldSyntax_rejectsInput() {
        for (String command : List.of("addattendance", "deleteattendance")) {
            for (String index : List.of("", "0", "-1", "1.5", "abc", "2147483648", "1 2",
                    "sn/A0123456X", "1 sn/A0123456X")) {
                assertThrows(ParseException.class, () -> parser.parseCommand(command + " " + index + ARGUMENTS));
            }
        }
    }

    private ModelManager assignedModel() {
        ModelManager model = new ModelManager();
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
        model.addAcademicClass(academicClass);
        model.assignStudentToClass(ALICE, academicClass);
        model.assignStudentToClass(BENSON, academicClass);
        return model;
    }
}
