package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises class lookup through the real command parser, model, and JSON storage. */
public class ListByStudentIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_editClassAfterStudentLookup_assignsToEditedClassAndRefreshesView() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        ModelManager model = new ModelManager();
        LogicManager logic = createLogic(model, storage);
        populate(logic);
        logic.execute("listbystudent 1");
        var displayed = model.getFilteredAcademicClassList();
        AcademicClass original = displayed.get(0);

        logic.execute("editclass 1 m/CS2113 c/RENAMED-T01");
        AcademicClass edited = model.findAcademicClass(new ModuleCode("CS2113"), new ClassName("RENAMED-T01"))
                .orElseThrow();
        assertNotSame(original, edited);
        assertSame(edited, displayed.get(0));
        assertEquals(List.of("CS2113-RENAMED-T01", "CS2100-DEMO-L01"), displayedClasses(model));
        assertEquals(original.getStudents(), edited.getStudents());

        assertThrows(CommandException.class, () -> logic.execute("assign sn/A0999803Z m/CS2103 c/DEMO-T01"));
        logic.execute("assign sn/A0999803Z m/CS2113 c/RENAMED-T01");
        assertEquals(3, edited.getStudents().size());
        assertEquals(2, original.getStudents().size());
        assertSame(edited, displayed.get(0));

        logic.execute("unassign 1 1");
        assertEquals(List.of("CS2100-DEMO-L01"), displayedClasses(model));
        logic.execute("assign sn/A0999801X m/CS2113 c/RENAMED-T01");
        assertEquals(List.of("CS2113-RENAMED-T01", "CS2100-DEMO-L01"), displayedClasses(model));
        assertSame(edited, displayed.get(0));
    }

    @Test
    public void execute_clearAfterStudentLookup_newClassIsVisible() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        ModelManager model = new ModelManager();
        LogicManager logic = createLogic(model, storage);
        populate(logic);
        logic.execute("listbystudent 1");
        assertEquals(List.of("CS2103-DEMO-T01", "CS2100-DEMO-L01"), displayedClasses(model));

        logic.execute("clear");
        assertEquals(List.of(), displayedClasses(model));
        assertEquals(List.of(), model.getFilteredPersonList());

        logic.execute("addclass m/CS2106 c/NEW-T01");
        assertEquals(List.of("CS2106-NEW-T01"), displayedClasses(model));
        assertEquals(1, storage.readAddressBook().orElseThrow().getAcademicClassList().size());
    }

    @Test
    public void execute_filteredStudentLookupAndRestart_preservesStoredDataAndMemberships() throws Exception {
        Path file = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ModelManager model = new ModelManager();
        LogicManager logic = createLogic(model, storage);
        populate(logic);
        String savedData = Files.readString(file);

        logic.execute("find AlphaDemoAlice");
        assertEquals("Listed 2 classes for AlphaDemoAlice (A0999801X).",
                logic.execute("listbystudent 1").getFeedbackToUser());
        assertEquals(List.of("CS2103-DEMO-T01", "CS2100-DEMO-L01"), displayedClasses(model));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(savedData, Files.readString(file));

        assertThrows(ParseException.class, () -> logic.execute("listbystudent 0"));
        assertThrows(CommandException.class, () -> logic.execute("listbystudent 2"));
        assertEquals(savedData, Files.readString(file));
        assertEquals(List.of("CS2103-DEMO-T01", "CS2100-DEMO-L01"), displayedClasses(model));

        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restarted = createLogic(reloaded, storage);
        assertEquals(3, reloaded.getFilteredAcademicClassList().size());
        restarted.execute("find AlphaDemoAlice");
        restarted.execute("listbystudent 1");
        assertEquals(List.of("CS2103-DEMO-T01", "CS2100-DEMO-L01"), displayedClasses(reloaded));

        restarted.execute("unassign 1 1");
        assertEquals(List.of("CS2100-DEMO-L01"), displayedClasses(reloaded));
        restarted.execute("assign sn/A0999801X m/CS2103 c/DEMO-T01");
        restarted.execute("edit 1 sn/A0999809Z");
        assertEquals(List.of("CS2103-DEMO-T01", "CS2100-DEMO-L01"), displayedClasses(reloaded));

        ModelManager afterEdit = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager afterEditLogic = createLogic(afterEdit, storage);
        afterEditLogic.execute("find AlphaDemoAlice");
        afterEditLogic.execute("listbystudent 1");
        assertEquals("A0999809Z", afterEdit.getFilteredPersonList().get(0).getStudentNumber().value);
        assertEquals(List.of("CS2103-DEMO-T01", "CS2100-DEMO-L01"), displayedClasses(afterEdit));

        restarted.execute("find AlphaDemoCharlie");
        restarted.execute("listbystudent 1");
        assertEquals(List.of(), displayedClasses(reloaded));
        restarted.execute("listclass");
        assertEquals(3, reloaded.getFilteredAcademicClassList().size());
    }

    private LogicManager createLogic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void populate(LogicManager logic) throws Exception {
        for (String command : List.of(
                "add sn/A0999801X n/AlphaDemoAlice e/alpha.demo.alice@example.com",
                "add sn/A0999802Y n/AlphaDemoBob e/alpha.demo.bob@example.com",
                "add sn/A0999803Z n/AlphaDemoCharlie e/alpha.demo.charlie@example.com",
                "addclass m/CS2103 c/DEMO-T01",
                "addclass m/CS2100 c/DEMO-L01",
                "addclass m/CS2103 c/DEMO-T02",
                "assign sn/A0999801X m/CS2103 c/DEMO-T01",
                "assign sn/A0999801X m/CS2100 c/DEMO-L01",
                "assign sn/A0999802Y m/CS2103 c/DEMO-T01")) {
            logic.execute(command);
        }
    }

    private List<String> displayedClasses(ModelManager model) {
        return model.getFilteredAcademicClassList().stream().map(AcademicClass::toString).toList();
    }
}
