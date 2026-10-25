package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks the assembled layout and existing executed-command behaviour with isolated storage. */
public class WorkspaceTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void initializeToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started the toolkit.
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void workspace_topCommandsAndAdjustableColumns_preserveWindowMinimum() throws Exception {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, createLogic());
                assertEquals(720, stage.getMinWidth());
                assertEquals(600, stage.getMinHeight());
                assertEquals("TAssist App", stage.getTitle());
                SplitPane split = (SplitPane) stage.getScene().lookup("#workspaceSplitPane");
                assertEquals(Orientation.HORIZONTAL, split.getOrientation());
                assertEquals(0.45, split.getDividerPositions()[0], 0.04);
                Node classes = split.getItems().get(0);
                Node students = split.getItems().get(1);
                assertTrue(classes.lookup("#academicClassListView") instanceof ListView);
                assertTrue(students.lookup("#personListView") instanceof ListView);
                assertTrue(classes.localToScene(classes.getBoundsInLocal()).getMaxX()
                        <= students.localToScene(students.getBoundsInLocal()).getMinX());
                Label classIndex = (Label) classes.lookup("#id");
                Label studentIndex = (Label) students.lookup("#id");
                assertEquals(classIndex.getText(), studentIndex.getText());
                assertEquals(classIndex.getFont(), studentIndex.getFont());
                assertEquals(classIndex.getTextFill(), studentIndex.getTextFill());
                assertEquals(classIndex.getPadding(), studentIndex.getPadding());
                Node className = classes.lookup("#className");
                Node studentName = students.lookup("#name");
                assertEquals(className.localToScene(0, 0).getX() - classIndex.localToScene(0, 0).getX(),
                        studentName.localToScene(0, 0).getX() - studentIndex.localToScene(0, 0).getX(), 0.5);
                Node input = stage.getScene().lookup("#commandTextField");
                Node output = stage.getScene().lookup("#resultDisplay");
                assertTrue(input.localToScene(input.getBoundsInLocal()).getMaxY()
                        <= output.localToScene(output.getBoundsInLocal()).getMinY());
                assertTrue(output.localToScene(output.getBoundsInLocal()).getMaxY()
                        <= split.localToScene(split.getBoundsInLocal()).getMinY());
                double previousClassWidth = classes.getBoundsInLocal().getWidth();
                double previousStudentWidth = students.getBoundsInLocal().getWidth();
                split.setDividerPositions(0.6);
                layout(stage);
                assertTrue(classes.getBoundsInLocal().getWidth() > previousClassWidth);
                assertTrue(students.getBoundsInLocal().getWidth() < previousStudentWidth);
                capture(stage, "layout-wide");
                split.setDividerPositions(0.45);
                stage.setWidth(740);
                settle(stage);
                capture(stage, "layout-compact");
                assertTrue(window.getPersonListPanel().getRoot().getWidth() > 200);
                stage.setWidth(720);
                settle(stage);
                assertTrue(split.getItems().get(1).getBoundsInLocal().getWidth() >= 220);
                capture(stage, "layout-minimum");
                split.setDividerPositions(0);
                settle(stage);
                Label classHeader = (Label) stage.getScene().lookup("#classHeader");
                assertTrue(classHeader.getHeight() > classHeader.getFont().getSize() * 1.5);
                assertTrue(classHeader.localToScene(classHeader.getBoundsInLocal()).getMaxX()
                        <= classes.localToScene(classes.getBoundsInLocal()).getMaxX());
                capture(stage, "usability-narrow-class-header");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void workspace_scaledSmallScreen_keepsCommandsAndBothListsUsable() throws Exception {
        runOnFxThreadAndWait(() -> {
            // 1280 x 720 at 150%, with a second case reserving space for desktop bars.
            for (double usableHeight : new double[] {720 / 1.5, 720 / 1.5 - 50}) {
                Rectangle2D screen = new Rectangle2D(50, 50, 1280 / 1.5, usableHeight);
                Stage stage = new Stage();
                try {
                    Logic logic = createLogic(30);
                    logic.setGuiSettings(new GuiSettings(1040, 760, 50, 50));
                    MainWindow window = new MainWindow(stage, logic, testFolder.resolve("addressbook.json"), () ->
                            List.of(screen), () -> screen);
                    window.fillInnerParts();
                    window.show();
                    settle(stage);
                    assertTrue(stage.getWidth() <= screen.getWidth() + 1);
                    assertTrue(stage.getHeight() <= screen.getHeight() + 1);
                    TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                    TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
                    ListView<?> classes = (ListView<?>) stage.getScene().lookup("#academicClassListView");
                    ListView<?> students = (ListView<?>) stage.getScene().lookup("#personListView");
                    for (Node control : List.of(input, output, classes, students)) {
                        var bounds = control.localToScene(control.getBoundsInLocal());
                        assertTrue(bounds.getWidth() > 0 && bounds.getHeight() > 0);
                        assertTrue(bounds.getMinX() >= 0 && bounds.getMinY() >= 0);
                        assertTrue(bounds.getMaxX() <= stage.getScene().getWidth() + 1);
                        assertTrue(bounds.getMaxY() <= stage.getScene().getHeight() + 1);
                    }
                    execute(input, "find Alice");
                    assertHeaders(stage, 32, 32, 1);
                    assertFalse(output.getText().isBlank());
                    execute(input, "list");
                    input.requestFocus();
                    press(input, KeyCode.TAB);
                    assertSame(output, stage.getScene().getFocusOwner());
                    press(output, KeyCode.TAB);
                    assertSame(classes, stage.getScene().getFocusOwner());
                    press(classes, KeyCode.END);
                    settle(stage);
                    VirtualFlow<?> classFlow = (VirtualFlow<?>) classes.lookup(".virtual-flow");
                    assertEquals(classes.getItems().size() - 1, classFlow.getLastVisibleCell().getIndex());
                    assertNull(classes.getSelectionModel());
                    press(classes, KeyCode.TAB);
                    assertSame(students, stage.getScene().getFocusOwner());
                    press(students, KeyCode.END);
                    settle(stage);
                    VirtualFlow<?> studentFlow = (VirtualFlow<?>) students.lookup(".virtual-flow");
                    assertEquals(students.getItems().size() - 1, studentFlow.getLastVisibleCell().getIndex());
                    assertEquals(3, students.getItems().size());
                    capture(stage, "usability-scaled-screen-" + (int) usableHeight);
                } finally {
                    stage.hide();
                }
            }
        });
    }

    @Test
    public void workspace_keyboardScrollsClasses_withoutSelectionOrDataChanges() throws Exception {
        runOnFxThreadAndWait(() -> {
            Logic logic = createLogic(30);
            Stage stage = new Stage();
            try {
                openWindow(stage, logic);
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
                ListView<?> classes = (ListView<?>) stage.getScene().lookup("#academicClassListView");
                ListView<?> students = (ListView<?>) stage.getScene().lookup("#personListView");
                List<?> originalStudents = List.copyOf(students.getItems());
                List<?> originalClasses = List.copyOf(classes.getItems());
                String originalOutput = output.getText();
                VirtualFlow<?> flow = (VirtualFlow<?>) classes.lookup(".virtual-flow");
                int initialFirstIndex = flow.getFirstVisibleCell().getIndex();

                input.requestFocus();
                press(input, KeyCode.TAB);
                assertSame(output, stage.getScene().getFocusOwner());
                press(output, KeyCode.TAB);
                assertSame(classes, stage.getScene().getFocusOwner());

                assertEquals(initialFirstIndex, flow.getFirstVisibleCell().getIndex());
                press(classes, KeyCode.HOME);
                settle(stage);
                assertEquals(0, flow.getFirstVisibleCell().getIndex());
                double beforeDownPosition = flow.getPosition();
                press(classes, KeyCode.DOWN);
                layout(stage);
                int afterDown = flow.getFirstVisibleCell().getIndex();
                assertTrue(flow.getPosition() > beforeDownPosition);
                press(classes, KeyCode.PAGE_DOWN);
                layout(stage);
                int afterPageDown = flow.getFirstVisibleCell().getIndex();
                assertTrue(afterPageDown > afterDown + 1);
                press(classes, KeyCode.PAGE_UP);
                layout(stage);
                assertTrue(flow.getFirstVisibleCell().getIndex() < afterPageDown);
                press(classes, KeyCode.END);
                layout(stage);
                assertEquals(classes.getItems().size() - 1, flow.getLastVisibleCell().getIndex());
                press(classes, KeyCode.PAGE_DOWN);
                layout(stage);
                assertEquals(classes.getItems().size() - 1, flow.getLastVisibleCell().getIndex());
                int beforeUp = flow.getFirstVisibleCell().getIndex();
                press(classes, KeyCode.UP);
                layout(stage);
                assertTrue(flow.getFirstVisibleCell().getIndex() < beforeUp);
                press(classes, KeyCode.HOME);
                layout(stage);
                assertEquals(0, flow.getFirstVisibleCell().getIndex());
                press(classes, KeyCode.UP);
                assertEquals(0, flow.getFirstVisibleCell().getIndex());
                assertSame(classes, stage.getScene().getFocusOwner());
                assertNull(classes.getSelectionModel());
                assertFalse(classes.lookupAll(".list-cell").stream()
                        .map(ListCell.class::cast).anyMatch(ListCell::isSelected));
                assertEquals(originalStudents, students.getItems());
                assertEquals(originalClasses, classes.getItems());
                assertEquals(originalOutput, output.getText());
                assertEquals("", input.getText());
                assertTrue(logic.getAttendanceDisplayClass().isEmpty());
                capture(stage, "layout-class-keyboard-focus");
                press(classes, KeyCode.TAB);
                assertSame(students, stage.getScene().getFocusOwner());
                press(students, KeyCode.TAB, true);
                assertSame(classes, stage.getScene().getFocusOwner());
                press(classes, KeyCode.TAB, true);
                assertSame(output, stage.getScene().getFocusOwner());

                execute(input, "clear");
                layout(stage);
                classes.requestFocus();
                for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN, KeyCode.PAGE_UP,
                        KeyCode.PAGE_DOWN, KeyCode.HOME, KeyCode.END)) {
                    press(classes, code);
                }
                assertTrue(classes.getItems().isEmpty());
                press(classes, KeyCode.TAB);
                assertSame(students, stage.getScene().getFocusOwner());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void workspace_draftsAndClassClicks_leaveDataUntilCommandExecutes() throws Exception {
        runOnFxThreadAndWait(() -> {
            Logic logic = createLogic();
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, logic);
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
                ListView<?> classes = (ListView<?>) stage.getScene().lookup("#academicClassListView");
                ListView<?> students = (ListView<?>) window.getPersonListPanel().getRoot().lookup("#personListView");
                assertEquals(Orientation.VERTICAL, classes.getOrientation());
                assertNull(classes.getSelectionModel());
                assertTrue(classes.isFocusTraversable());
                assertSame(logic.getFilteredPersonList(), students.getItems());
                assertSame(logic.getFilteredAcademicClassList(), classes.getItems());
                List<?> originalStudents = List.copyOf(students.getItems());
                List<?> originalClasses = List.copyOf(classes.getItems());
                String originalOutput = output.getText();
                for (String draft : List.of("find Alice", "listbyclass 1", "listbystudent 1",
                        "addattendance 1 1 d/2020-01-03", "listbyclass 9999", "clear", "")) {
                    input.setText(draft);
                    assertEquals(originalStudents, students.getItems());
                    assertEquals(originalClasses, classes.getItems());
                    assertEquals(originalOutput, output.getText());
                    assertTrue(logic.getAttendanceDisplayClass().isEmpty());
                }
                Node row = classes.lookup("#cardPane");
                row.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, MouseButton.PRIMARY,
                        1, false, false, false, false, true, false, false, true, false, true, null));
                assertEquals(originalStudents, students.getItems());
                assertEquals(originalOutput, output.getText());
                assertFalse(classes.lookupAll(".list-cell").stream()
                        .anyMatch(cell -> cell.getPseudoClassStates().stream()
                                .anyMatch(state -> state.getPseudoClassName().equals("selected"))));

                execute(input, "find Alice");
                assertEquals(List.of(ALICE), students.getItems());
                execute(input, "list");
                assertEquals(originalStudents, students.getItems());
                execute(input, "listbyclass 1");
                assertEquals(List.of(ALICE, BENSON), students.getItems());
                layout(stage);
                assertLabelExists(students, "2020-01-01, 2020-01-02");
                assertLabelExists(students, "No attendance recorded");
                String classOutput = output.getText();
                input.setText("listbyclass 2");
                assertEquals(List.of(ALICE, BENSON), students.getItems());
                assertEquals(classOutput, output.getText());
                layout(stage);
                assertLabelExists(students, "2020-01-01, 2020-01-02");
                execute(input, "listbyclass 9999");
                assertEquals(List.of(ALICE, BENSON), students.getItems());
                assertTrue(input.getStyleClass().contains("error"));
                execute(input, "listbyclass 1 t/2");
                assertEquals(List.of(BENSON), students.getItems());
                layout(stage);
                assertLabelExists(students, "1. ");
                execute(input, "listbyclass 1");
                execute(input, "addattendance 2 1 d/2020-01-03");
                layout(stage);
                assertLabelExists(students, "2020-01-03");
                capture(stage, "layout-attendance");
                execute(input, "deleteattendance 2 1 d/2020-01-03");
                layout(stage);
                assertLabelExists(students, "No attendance recorded");
                execute(input, "list");
                execute(input, "listbystudent 3");
                assertEquals(List.of(originalClasses.get(1)), classes.getItems());
                execute(input, "listclass");
                assertEquals(originalClasses, classes.getItems());
                execute(input, "add sn/DEMO0001 n/Ella With A Very Long Student Name "
                        + "e/ella.with.a.long.email.address@example.com "
                        + "tg/wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww "
                        + "t/aVeryLongTagThatMustWrapWithinTheStudentCard");
                stage.setWidth(720);
                settle(stage);
                capture(stage, "layout-long-details");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void workspace_headersAndEmptyStates_followExecutedCommands() throws Exception {
        runOnFxThreadAndWait(() -> {
            Logic logic = createLogic(1);
            Stage stage = new Stage();
            try {
                openWindow(stage, logic);
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                ListView<?> classes = (ListView<?>) stage.getScene().lookup("#academicClassListView");
                ListView<?> students = (ListView<?>) stage.getScene().lookup("#personListView");
                assertHeaders(stage, 3, 3, 3);
                for (String draft : List.of("find Alice", "listbystudent 1", "listbyclass 3")) {
                    input.setText(draft);
                    assertHeaders(stage, 3, 3, 3);
                }
                execute(input, "listbystudent 1");
                assertHeaders(stage, 1, 3, 3);
                execute(input, "assign sn/A0123456X m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME");
                assertHeaders(stage, 2, 3, 3);
                execute(input, "addclass m/CS2100 c/EMPTY");
                assertHeaders(stage, 2, 4, 3);
                capture(stage, "usability-filtered-counts");
                execute(input, "unassign 1 2");
                assertHeaders(stage, 1, 4, 3);
                execute(input, "edit 1 sn/A1111111X");
                assertHeaders(stage, 1, 4, 3);
                execute(input, "deleteclass m/CS2103 c/F10");
                assertHeaders(stage, 0, 3, 3);
                layout(stage);
                assertLabelExists(classes, "No classes shown.");
                assertTrue(classes.lookup(".placeholder").isVisible());
                execute(input, "addclass m/CS2103 c/F12");
                assertHeaders(stage, 0, 4, 3);
                execute(input, "listbyclass 9999");
                assertHeaders(stage, 0, 4, 3);
                execute(input, "listclass");
                assertHeaders(stage, 4, 4, 3);
                execute(input, "find Nobody");
                assertHeaders(stage, 4, 4, 0);
                layout(stage);
                assertLabelExists(students, "No students shown.");
                assertTrue(students.lookup(".placeholder").isVisible());
                execute(input, "list");
                assertHeaders(stage, 4, 4, 3);
                execute(input, "add sn/DEMO0001 n/Ella Ng e/ella@example.com");
                assertHeaders(stage, 4, 4, 4);
                execute(input, "find Ella");
                assertHeaders(stage, 4, 4, 1);
                execute(input, "delete 1");
                assertHeaders(stage, 4, 4, 0);
                execute(input, "list");
                assertHeaders(stage, 4, 4, 3);
                execute(input, "listbyclass 3");
                assertHeaders(stage, 4, 4, 0);
                execute(input, "clear");
                assertHeaders(stage, 0, 0, 0);
                layout(stage);
                assertLabelExists(classes, "No classes shown.");
                assertLabelExists(students, "No students shown.");
                capture(stage, "usability-empty");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void workspace_successfulClassIndexCommands_revealOnlyApplicableTargets() throws Exception {
        runOnFxThreadAndWait(() -> {
            Logic logic = createLogic(30);
            Stage stage = new Stage();
            try {
                openWindow(stage, logic);
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                ListView<?> classes = (ListView<?>) stage.getScene().lookup("#academicClassListView");
                VirtualFlow<?> flow = (VirtualFlow<?>) classes.lookup(".virtual-flow");
                List<Integer> scrollTargets = new ArrayList<>();
                classes.addEventHandler(ScrollToEvent.scrollToTopIndex(),
                        event -> scrollTargets.add(event.getScrollTarget()));
                int initialFirstIndex = flow.getFirstVisibleCell().getIndex();
                input.setText("listbyclass 32");
                assertTrue(scrollTargets.isEmpty());
                assertEquals(initialFirstIndex, flow.getFirstVisibleCell().getIndex());
                assertHeaders(stage, 32, 32, 3);
                for (String invalid : List.of("listbyclass 9999", "listbyclass", "listbyclass abc",
                        "editclass 999999999999999999 c/INVALID")) {
                    execute(input, invalid);
                    assertTrue(scrollTargets.isEmpty());
                }
                execute(input, "listbyclass 32");
                settle(stage);
                assertEquals(List.of(31), scrollTargets);
                assertEquals(31, flow.getLastVisibleCell().getIndex());
                assertHeaders(stage, 32, 32, 0);
                press(classes, KeyCode.HOME);
                execute(input, "  editclass   32   c/TARGET-RENAMED  ");
                settle(stage);
                assertEquals(List.of(31, 31), scrollTargets);
                assertEquals("TARGET-RENAMED", logic.getAcademicClassList().get(31).getClassName().value);
                assertEquals(31, flow.getLastVisibleCell().getIndex());

                press(classes, KeyCode.HOME);
                settle(stage);
                scrollTargets.clear();
                execute(input, "assign sn/A0123456X m/CS2103 c/TARGET-RENAMED");
                execute(input, "list");
                assertTrue(scrollTargets.isEmpty());
                assertEquals(0, flow.getFirstVisibleCell().getIndex());
                execute(input, "addattendance 1 32 d/2020-01-03");
                settle(stage);
                assertEquals(List.of(31), scrollTargets);
                assertEquals(31, flow.getLastVisibleCell().getIndex());
                press(classes, KeyCode.HOME);
                execute(input, "deleteattendance 1 32 d/2020-01-03");
                settle(stage);
                assertEquals(List.of(31, 31), scrollTargets);
                assertEquals(31, flow.getLastVisibleCell().getIndex());

                execute(input, "listbystudent 1");
                assertHeaders(stage, 2, 32, 3);
                scrollTargets.clear();
                execute(input, "unassign 1 2");
                assertHeaders(stage, 1, 32, 3);
                assertTrue(scrollTargets.isEmpty(), "A class removed by the active filter must not reveal another row");
                execute(input, "listclass");
                press(classes, KeyCode.END);
                scrollTargets.clear();
                execute(input, "unassign 1 1");
                settle(stage);
                assertEquals(List.of(0), scrollTargets);
                assertEquals(0, flow.getFirstVisibleCell().getIndex());
                scrollTargets.clear();
                execute(input, "addattendance 1 32 d/2020-01-03");
                assertTrue(scrollTargets.isEmpty(), "Failed commands must not scroll a valid class target");
                assertEquals(0, flow.getFirstVisibleCell().getIndex());
                assertNull(classes.getSelectionModel());
            } finally {
                stage.hide();
            }
        });
    }

    private static void assertHeaders(Stage stage, int shownClasses, int totalClasses, int shownStudents) {
        Label classHeader = (Label) stage.getScene().lookup("#classHeader");
        Label studentHeader = (Label) stage.getScene().lookup("#studentHeader");
        assertEquals("Classes — " + shownClasses + " shown / " + totalClasses + " total",
                classHeader.getText());
        assertEquals("Students — " + shownStudents + " shown", studentHeader.getText());
    }

    private MainWindow openWindow(Stage stage, Logic logic) {
        logic.setGuiSettings(new GuiSettings(1040, 760, 0, 0));
        MainWindow window = new MainWindow(stage, logic, testFolder.resolve("addressbook.json"));
        window.fillInnerParts();
        window.show();
        settle(stage);
        return window;
    }

    private Logic createLogic() {
        return createLogic(0);
    }

    private Logic createLogic(int extraClassCount) {
        AddressBook book = new AddressBook();
        for (var student : List.of(ALICE, BENSON, DANIEL)) {
            book.addPerson(student);
        }
        AcademicClass first = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10"));
        AcademicClass second = new AcademicClass(new ModuleCode("CS2101"),
                new ClassName("TUTORIAL-GROUP-WITH-A-LONG-NAME"));
        book.addAcademicClass(first);
        book.addAcademicClass(second);
        for (int index = 0; index < extraClassCount; index++) {
            book.addAcademicClass(new AcademicClass(new ModuleCode("CS2103"), new ClassName("T" + index)));
        }
        book.assignStudentToClass(ALICE, first);
        book.assignStudentToClass(BENSON, first);
        book.assignStudentToClass(DANIEL, second);
        book.addAttendance(ALICE, first, LocalDate.of(2020, 1, 1));
        book.addAttendance(ALICE, first, LocalDate.of(2020, 1, 2));
        return new LogicManager(new ModelManager(book, new UserPrefs()), new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    private static void execute(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static void press(Node target, KeyCode code) {
        press(target, code, false);
    }

    private static void press(Node target, KeyCode code, boolean shiftDown) {
        target.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shiftDown, false, false, false));
    }

    private static void assertLabelExists(Node root, String text) {
        assertTrue(root.lookupAll(".label").stream().filter(Label.class::isInstance)
                .map(Label.class::cast).anyMatch(label -> label.getText().equals(text)), text);
    }

    private static void layout(Stage stage) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
    }

    private static void settle(Stage stage) {
        // Allow native resizing and control skins to settle before measuring or capturing.
        Object frame = new Object();
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(event -> Platform.exitNestedEventLoop(frame, null));
        pause.play();
        Platform.enterNestedEventLoop(frame);
        layout(stage);
    }

    private static void capture(Stage stage, String name) {
        if (!"1".equals(System.getenv("TASSIST_CAPTURE_UI"))) {
            return;
        }
        settle(stage);
        WritableImage snapshot = stage.getScene().snapshot(null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();
        int[] pixels = new int[width * height];
        snapshot.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        try {
            Path directory = Path.of("build", "ui-preview");
            Files.createDirectories(directory);
            ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
