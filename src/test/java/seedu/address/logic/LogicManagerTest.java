package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.STUDENT_NUMBER_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_blankInput_rejectsWithoutChangingHistoryOrSavedData() throws Exception {
        logic.execute("list");
        Path savedFile = temporaryFolder.resolve("addressBook.json");
        String savedContents = Files.readString(savedFile);
        for (String blank : List.of("", " ", "\t\r\n")) {
            assertThrows(ParseException.class, () -> logic.execute(blank));
            assertEquals(List.of("list"), logic.getCommandHistory());
            assertEquals(savedContents, Files.readString(savedFile));
        }
    }

    @Test
    public void execute_history_survivesRestartAndClearAndExcludesFailures() throws Exception {
        logic.execute("help");
        logic.execute("list");
        assertThrows(ParseException.class, () -> logic.execute("invalid"));
        assertThrows(CommandException.class, () -> logic.execute("delete 999"));
        logic.execute("clear");
        List<String> expected = List.of("help", "list", "clear");
        assertEquals(expected, logic.getCommandHistory());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(expected, restarted.getAddressBook().getCommandHistory());
    }

    @Test
    public void execute_history_keepsLatestTwentyAcrossRestart() throws Exception {
        for (int i = 0; i < 25; i++) {
            logic.execute(i % 2 == 0 ? "help" : "list");
        }
        List<String> history = logic.getCommandHistory();
        assertEquals(20, history.size());
        assertEquals("list", history.getFirst());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(history, storage.readAddressBook().orElseThrow().getCommandHistory());
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_addAttendance_savesHistoryAndRejectsInvalidChanges() throws Exception {
        logic.execute("add sn/A0123456X n/Alice e/alice@example.com");
        logic.execute("addclass m/CS2103 c/F10-2");
        logic.execute("assign 1 1");
        String attendanceCommand = "addattendance 1 1 d/2024-02-29";
        logic.execute(attendanceCommand);

        Path savedFile = temporaryFolder.resolve("addressBook.json");
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(savedFile).readAddressBook().orElseThrow();
        assertEquals(1, reloaded.getAttendanceList().size());
        assertEquals(model.getAddressBook(), reloaded);
        String savedContents = Files.readString(savedFile);

        assertThrows(CommandException.class, () -> logic.execute(attendanceCommand));
        String invalidDateCommand = attendanceCommand.replace("2024-02-29", "2024-02-30");
        String futureDateCommand = attendanceCommand.replace("2024-02-29", "9999-12-31");
        assertThrows(ParseException.class, () -> logic.execute(invalidDateCommand));
        assertThrows(CommandException.class, () -> logic.execute(futureDateCommand));
        assertEquals(savedContents, Files.readString(savedFile));
        assertEquals(reloaded, model.getAddressBook());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_deleteAttendanceWhileUnassigned_persistsRemainingDate() throws Exception {
        logic.execute("add sn/A0123456X n/Alice e/alice@example.com");
        logic.execute("addclass m/CS2103 c/F10-2");
        logic.execute("assign 1 1");
        logic.execute("addattendance 1 1 d/2024-02-28");
        logic.execute("addattendance 1 1 d/2024-02-29");
        logic.execute("unassign 1 1");
        String deleteCommand = "deleteattendance 1 1 d/2024-02-29";
        logic.execute(deleteCommand);

        Path savedFile = temporaryFolder.resolve("addressBook.json");
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(savedFile).readAddressBook().orElseThrow();
        assertEquals(Set.of(LocalDate.of(2024, 2, 28)),
                reloaded.getAttendanceList().getFirst().getAttendedDates());
        assertEquals(Set.of(), reloaded.getAcademicClassList().getFirst().getStudents());
        assertEquals(model.getAddressBook(), reloaded);
        String savedContents = Files.readString(savedFile);
        assertThrows(CommandException.class, () -> logic.execute(deleteCommand));
        String invalidDateCommand = deleteCommand.replace("2024-02-29", "2024-02-30");
        assertThrows(ParseException.class, () -> logic.execute(invalidDateCommand));
        assertEquals(savedContents, Files.readString(savedFile));
        assertEquals(reloaded, model.getAddressBook());
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void getAcademicClassList_returnsModelAcademicClassList() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        model.addAcademicClass(academicClass);
        assertEquals(List.of(academicClass), logic.getAcademicClassList());
    }

    @Test
    public void getAcademicClassList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getAcademicClassList().remove(0));
    }

    @Test
    public void getFilteredAcademicClassList_returnsModelFilteredClassList() {
        assertEquals(model.getFilteredAcademicClassList(), logic.getFilteredAcademicClassList());
    }

    @Test
    public void getAttendanceDetails_delegatesToModel() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
        model.addPerson(AMY);
        model.addAcademicClass(academicClass);
        model.assignStudentToClass(AMY, academicClass);
        model.addAttendance(AMY, academicClass, LocalDate.of(2020, 1, 1));
        model.setAttendanceDisplayClass(academicClass);

        assertEquals(java.util.Optional.of(academicClass), logic.getAttendanceDisplayClass());
        assertEquals(model.getAttendanceList(), logic.getAttendanceList());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + STUDENT_NUMBER_DESC_AMY + NAME_DESC_AMY
                + EMAIL_DESC_AMY + TELEGRAM_DESC_AMY;
        // The failed command must be rolled back, leaving the model unchanged.
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, new ModelManager());
        assertEquals(List.of(), logic.getCommandHistory());
    }

    @Test
    public void execute_storageFailsAfterAssign_rollsBackClassMembership() throws Exception {
        logic.execute("add sn/A0123456X n/Alice e/alice@example.com");
        logic.execute("addclass m/CS2103 c/F10-2");
        ReadOnlyAddressBook before = ((AddressBook) model.getAddressBook()).deepCopy();

        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(
                temporaryFolder.resolve("failing.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        }, new JsonUserPrefsStorage(temporaryFolder.resolve("failingPrefs.json"))));

        assertThrows(CommandException.class, () -> logic.execute("assign 1 1"));
        assertEquals(before, model.getAddressBook());
        assertEquals(Set.of(), model.getAddressBook().getAcademicClassList().getFirst().getStudents());
    }

    @Test
    public void execute_storageFails_rollbackKeepsStudentFilter() throws Exception {
        logic.execute("add sn/A0123456X n/Alice e/alice@example.com");
        logic.execute("add sn/A0234567X n/Bob e/bob@example.com");
        logic.execute("find Bob");
        List<Person> filteredBefore = List.copyOf(model.getFilteredPersonList());
        assertEquals(1, filteredBefore.size());

        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(
                temporaryFolder.resolve("failing.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        }, new JsonUserPrefsStorage(temporaryFolder.resolve("failingPrefs.json"))));

        assertThrows(CommandException.class, () -> logic.execute("addclass m/CS2103 c/F10-2"));
        assertEquals(filteredBefore, model.getFilteredPersonList());
    }

    @Test
    public void execute_storageFailsAfterDelete_rollsBackStudentAndMemberships() throws Exception {
        assertStorageFailureRollsBack(null, "delete 1");
    }

    @Test
    public void execute_storageFailsAfterEdit_rollsBackStudent() throws Exception {
        assertStorageFailureRollsBack(null, "edit 1 n/Zed");
    }

    @Test
    public void execute_storageFailsAfterClear_rollsBackAllData() throws Exception {
        assertStorageFailureRollsBack(null, "clear");
    }

    @Test
    public void execute_storageFailsAfterDeleteClass_rollsBackClassAndAttendance() throws Exception {
        assertStorageFailureRollsBack(null, "deleteclass 1");
    }

    @Test
    public void execute_storageFailsAfterEditClass_rollsBackClassIdentity() throws Exception {
        assertStorageFailureRollsBack(null, "editclass 1 c/T99");
    }

    @Test
    public void execute_storageFailsAfterUnassign_rollsBackMembership() throws Exception {
        assertStorageFailureRollsBack(null, "unassign 1 1");
    }

    @Test
    public void execute_storageFailsAfterAddAttendance_rollsBackAttendance() throws Exception {
        assertStorageFailureRollsBack(null, "addattendance 1 2 d/2020-02-02");
    }

    @Test
    public void execute_storageFailsAfterDeleteAttendance_rollsBackAttendance() throws Exception {
        assertStorageFailureRollsBack(null, "deleteattendance 1 1 d/2020-01-01");
    }

    @Test
    public void execute_storageFailsWithClassFilter_rollbackKeepsClassFilterAndAttendanceDisplay()
            throws Exception {
        assertStorageFailureRollsBack("listbyclass 1", "edit 1 n/Zed");
        assertEquals("CS2103", model.getAttendanceDisplayClass().orElseThrow().getModuleCode().toString());
    }

    @Test
    public void execute_storageFailsAfterEditClassWithClassFilter_rollbackKeepsOriginalClassFilter()
            throws Exception {
        assertStorageFailureRollsBack("listbyclass 1", "editclass 1 c/T99");
        assertEquals("F10-2", model.getAttendanceDisplayClass().orElseThrow().getClassName().toString());
    }

    @Test
    public void execute_storageFailsWithStudentClassFilter_rollbackKeepsClassList() throws Exception {
        assertStorageFailureRollsBack("listbystudent 2", "deleteclass 1");
    }

    @Test
    public void execute_storageFailsWithSortedList_rollbackKeepsOrder() throws Exception {
        assertStorageFailureRollsBack("find Bob", "add sn/A0345678X n/Carol e/carol@example.com");
    }

    @Test
    public void execute_accessDeniedAfterEditClass_rollsBackAndReportsPermissionError() throws Exception {
        prepareStateForRollbackTests(null);
        ReadOnlyAddressBook before = ((AddressBook) model.getAddressBook()).deepCopy();
        useStorageThatThrows(DUMMY_AD_EXCEPTION);

        CommandException e = Assertions.assertThrows(CommandException.class, () -> logic.execute("editclass 1 c/T99"));
        assertEquals(String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()),
                e.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    /**
     * Sets up two students, two classes, memberships and attendance, optionally applies {@code displayCommand},
     * then checks that {@code failingCommand} is rolled back when saving fails: the data, history and displayed
     * lists must all match what they were before the command.
     */
    private void assertStorageFailureRollsBack(String displayCommand, String failingCommand) throws Exception {
        prepareStateForRollbackTests(displayCommand);
        ReadOnlyAddressBook dataBefore = ((AddressBook) model.getAddressBook()).deepCopy();
        List<Person> personsBefore = List.copyOf(model.getFilteredPersonList());
        List<AcademicClass> classesBefore = List.copyOf(model.getFilteredAcademicClassList());
        List<String> historyBefore = List.copyOf(logic.getCommandHistory());
        useStorageThatThrows(DUMMY_IO_EXCEPTION);

        assertThrows(CommandException.class, () -> logic.execute(failingCommand));

        assertEquals(dataBefore, model.getAddressBook());
        assertEquals(personsBefore, model.getFilteredPersonList());
        assertEquals(classesBefore, model.getFilteredAcademicClassList());
        assertEquals(historyBefore, logic.getCommandHistory());
    }

    private void prepareStateForRollbackTests(String displayCommand) throws Exception {
        logic.execute("add sn/A0123456X n/Alice e/alice@example.com");
        logic.execute("add sn/A0234567X n/Bob e/bob@example.com");
        logic.execute("addclass m/CS2103 c/F10-2");
        logic.execute("addclass m/ST2334 c/T24");
        logic.execute("assign 1 1");
        logic.execute("assign 1 2");
        logic.execute("assign 2 2");
        logic.execute("addattendance 1 1 d/2020-01-01");
        logic.execute("addattendance 2 2 d/2020-01-02");
        if (displayCommand != null) {
            logic.execute(displayCommand);
        }
    }

    private void useStorageThatThrows(IOException exception) {
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(
                temporaryFolder.resolve("failing.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw exception;
            }
        }, new JsonUserPrefsStorage(temporaryFolder.resolve("failingPrefs.json"))));
    }
}
