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
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
import seedu.address.testutil.PersonBuilder;

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
                Node students = split.getItems().get(0);
                Node classes = split.getItems().get(1);
                assertTrue(classes.lookup("#academicClassListView") instanceof ListView);
                assertTrue(students.lookup("#personListView") instanceof ListView);
                assertTrue(students.localToScene(students.getBoundsInLocal()).getMaxX()
                        <= classes.localToScene(classes.getBoundsInLocal()).getMinX());
                assertTrue(classes.getBoundsInLocal().getWidth() > students.getBoundsInLocal().getWidth());
                VirtualFlow<?> classFlow = (VirtualFlow<?>) classes.lookup(".virtual-flow");
                double wideClassHeight = classFlow.getVisibleCell(1).getHeight();
                assertHeaderLayout(stage, "student");
                assertHeaderLayout(stage, "class");
                Label classIndex = (Label) classes.lookup("#id");
                Label studentIndex = (Label) students.lookup("#id");
                assertEquals(classIndex.getText(), studentIndex.getText());
                assertEquals(classIndex.getFont(), studentIndex.getFont());
                assertEquals(classIndex.getTextFill(), studentIndex.getTextFill());
                assertEquals(classIndex.getPadding(), studentIndex.getPadding());
                Node classHeading = classes.lookup("#classHeading");
                Text className = (Text) classes.lookup("#className");
                Label studentName = (Label) students.lookup("#name");
                Text moduleCode = (Text) classes.lookup("#moduleCode");
                assertEquals(classHeading.localToScene(0, 0).getX() - classIndex.localToScene(0, 0).getX(),
                        studentName.localToScene(0, 0).getX() - studentIndex.localToScene(0, 0).getX(), 0.5);
                assertEquals(studentName.getFont(), className.getFont());
                assertEquals(studentName.getFont(), moduleCode.getFont());
                assertEquals(14, studentName.getFont().getSize());
                for (String detail : List.of("id", "studentNumber", "email", "telegram", "attendance")) {
                    assertNormalCardFont((Label) students.lookup("#" + detail));
                }
                for (Node tag : students.lookup("#tags").lookupAll(".label")) {
                    assertNormalCardFont((Label) tag);
                }
                assertNormalCardFont(classIndex);
                assertNormalCardFont((Label) classes.lookup("#studentCount"));
                assertEquals(classHeading.localToScene(0, 0).getX(),
                        classes.lookup("#studentCount").localToScene(0, 0).getX(), 0.5);
                for (String detail : List.of("details", "tags")) {
                    assertEquals(studentName.localToScene(0, 0).getX(),
                            students.lookup("#" + detail).localToScene(0, 0).getX(), 0.5);
                }
                Node input = stage.getScene().lookup("#commandTextField");
                Node output = stage.getScene().lookup("#resultDisplay");
                assertTrue(input.localToScene(input.getBoundsInLocal()).getMaxY()
                        <= output.localToScene(output.getBoundsInLocal()).getMinY());
                assertTrue(output.localToScene(output.getBoundsInLocal()).getMaxY()
                        <= split.localToScene(split.getBoundsInLocal()).getMinY());
                double previousClassWidth = classes.getBoundsInLocal().getWidth();
                double previousStudentWidth = students.getBoundsInLocal().getWidth();
                split.setDividerPositions(0.3);
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
                assertTrue(students.getBoundsInLocal().getWidth() >= 220);
                capture(stage, "layout-minimum");
                split.setDividerPositions(1);
                settle(stage);
                Label classCount = (Label) stage.getScene().lookup("#classCount");
                assertTrue(classCount.getHeight() > classCount.getFont().getSize() * 2);
                assertHeaderLayout(stage, "student");
                assertHeaderLayout(stage, "class");
                capture(stage, "usability-narrow-class-header");
                assertTrue(classFlow.getVisibleCell(1).getHeight() > wideClassHeight);
                split.setDividerPositions(0);
                settle(stage);
                assertHeaderLayout(stage, "student");
                assertHeaderLayout(stage, "class");
                assertEquals(wideClassHeight, classFlow.getVisibleCell(1).getHeight(), 1,
                        "Rendered class rows must shrink again when the divider moves back");
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
                    assertHeaders(stage, 32, 32, 1, 3);
                    assertFalse(output.getText().isBlank());
                    execute(input, "list");
                    settle(stage);
                    input.requestFocus();
                    press(input, KeyCode.TAB);
                    assertSame(output, stage.getScene().getFocusOwner());
                    press(output, KeyCode.TAB);
                    assertSame(students, stage.getScene().getFocusOwner());
                    press(students, KeyCode.END);
                    settle(stage);
                    VirtualFlow<?> studentFlow = (VirtualFlow<?>) students.lookup(".virtual-flow");
                    assertEquals(students.getItems().size() - 1, studentFlow.getLastVisibleCell().getIndex());
                    assertTrue(studentFlow.getLastVisibleCell().getBoundsInParent().getMaxY()
                            <= studentFlow.getHeight() + 1, "End must reveal the whole last student card");
                    assertEquals(3, students.getItems().size());
                    assertNull(students.getSelectionModel());
                    press(students, KeyCode.TAB);
                    assertSame(classes, stage.getScene().getFocusOwner());
                    press(classes, KeyCode.END);
                    settle(stage);
                    VirtualFlow<?> classFlow = (VirtualFlow<?>) classes.lookup(".virtual-flow");
                    assertEquals(classes.getItems().size() - 1, classFlow.getLastVisibleCell().getIndex());
                    assertNull(classes.getSelectionModel());
                    capture(stage, "usability-scaled-screen-" + (int) usableHeight);
                } finally {
                    stage.hide();
                }
            }
        });
    }

    @Test
    public void workspace_keyboardAndMouseScrollBothLists_withoutSelectionOrDataChanges() throws Exception {
        runOnFxThreadAndWait(() -> {
            Logic logic = createLogic(30, 30);
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

                input.requestFocus();
                press(input, KeyCode.TAB);
                assertSame(output, stage.getScene().getFocusOwner());
                press(output, KeyCode.TAB);
                assertSame(students, stage.getScene().getFocusOwner());
                press(students, KeyCode.TAB);
                assertSame(classes, stage.getScene().getFocusOwner());

                for (ListView<?> list : List.of(students, classes)) {
                    assertListScrollsWithoutSelection(stage, list);
                }
                assertEquals(originalStudents, students.getItems());
                assertEquals(originalClasses, classes.getItems());
                assertEquals(originalOutput, output.getText());
                assertEquals("", input.getText());
                assertTrue(logic.getAttendanceDisplayClass().isEmpty());
                capture(stage, "layout-class-keyboard-focus");
                press(classes, KeyCode.TAB);
                assertSame(input, stage.getScene().getFocusOwner());
                press(input, KeyCode.TAB, true);
                assertSame(classes, stage.getScene().getFocusOwner());
                press(classes, KeyCode.TAB, true);
                assertSame(students, stage.getScene().getFocusOwner());
                press(students, KeyCode.TAB, true);
                assertSame(output, stage.getScene().getFocusOwner());

                execute(input, "clear");
                layout(stage);
                for (ListView<?> list : List.of(students, classes)) {
                    list.requestFocus();
                    for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN, KeyCode.PAGE_UP,
                            KeyCode.PAGE_DOWN, KeyCode.HOME, KeyCode.END)) {
                        press(list, code);
                    }
                    assertTrue(list.getItems().isEmpty());
                    assertNull(list.getSelectionModel());
                }
                press(classes, KeyCode.TAB);
                assertSame(input, stage.getScene().getFocusOwner());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void workspace_draftsAndCardClicks_leaveDataUntilCommandExecutes() throws Exception {
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
                for (ListView<?> list : List.of(students, classes)) {
                    assertNull(list.getSelectionModel());
                    assertTrue(list.isFocusTraversable());
                    click(list.lookup("#cardPane"));
                    assertFalse(list.lookupAll(".list-cell").stream()
                            .map(ListCell.class::cast).anyMatch(ListCell::isSelected));
                }
                assertEquals(originalStudents, students.getItems());
                assertEquals(originalOutput, output.getText());

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
                assertHeaders(stage, 3, 3, 3, 3);
                for (String draft : List.of("find Alice", "listbystudent 1", "listbyclass 3")) {
                    input.setText(draft);
                    assertHeaders(stage, 3, 3, 3, 3);
                }
                execute(input, "listbystudent 1");
                assertHeaders(stage, 1, 3, 3, 3);
                execute(input, "assign sn/A0123456X m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME");
                assertHeaders(stage, 2, 3, 3, 3);
                execute(input, "addclass m/CS2100 c/EMPTY");
                assertHeaders(stage, 2, 4, 3, 3);
                capture(stage, "usability-filtered-counts");
                execute(input, "unassign 1 2");
                assertHeaders(stage, 1, 4, 3, 3);
                execute(input, "edit 1 sn/A1111111X");
                assertHeaders(stage, 1, 4, 3, 3);
                execute(input, "deleteclass m/CS2103 c/F10");
                assertHeaders(stage, 0, 3, 3, 3);
                layout(stage);
                assertLabelExists(classes, "No classes shown.");
                assertTrue(classes.lookup(".placeholder").isVisible());
                execute(input, "addclass m/CS2103 c/F12");
                assertHeaders(stage, 0, 4, 3, 3);
                execute(input, "listbyclass 9999");
                assertHeaders(stage, 0, 4, 3, 3);
                execute(input, "listclass");
                assertHeaders(stage, 4, 4, 3, 3);
                execute(input, "find Nobody");
                assertHeaders(stage, 4, 4, 0, 3);
                layout(stage);
                assertLabelExists(students, "No students shown.");
                assertTrue(students.lookup(".placeholder").isVisible());
                execute(input, "list");
                assertHeaders(stage, 4, 4, 3, 3);
                execute(input, "add sn/DEMO0001 n/Ella Ng e/ella@example.com");
                assertHeaders(stage, 4, 4, 4, 4);
                execute(input, "find Ella");
                assertHeaders(stage, 4, 4, 1, 4);
                execute(input, "delete 1");
                assertHeaders(stage, 4, 4, 0, 3);
                execute(input, "list");
                assertHeaders(stage, 4, 4, 3, 3);
                execute(input, "listbyclass 3");
                assertHeaders(stage, 4, 4, 0, 3);
                execute(input, "clear");
                assertHeaders(stage, 0, 0, 0, 0);
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
                assertHeaders(stage, 32, 32, 3, 3);
                for (String invalid : List.of("listbyclass 9999", "listbyclass", "listbyclass abc",
                        "editclass 999999999999999999 c/INVALID")) {
                    execute(input, invalid);
                    assertTrue(scrollTargets.isEmpty());
                }
                execute(input, "listbyclass 32");
                settle(stage);
                assertEquals(List.of(31), scrollTargets);
                assertEquals(31, flow.getLastVisibleCell().getIndex());
                assertHeaders(stage, 32, 32, 0, 3);
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
                assertHeaders(stage, 2, 32, 3, 3);
                scrollTargets.clear();
                execute(input, "unassign 1 2");
                assertHeaders(stage, 1, 32, 3, 3);
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

    private static void assertListScrollsWithoutSelection(Stage stage, ListView<?> list) {
        list.requestFocus();
        press(list, KeyCode.HOME);
        settle(stage);
        // Check focus styling even when the window manager has not granted this stage native focus.
        PseudoClass focused = PseudoClass.getPseudoClass("focused");
        list.pseudoClassStateChanged(focused, true);
        list.applyCss();
        assertEquals(Color.web("#91b4d3"), list.getBorder().getStrokes().get(0).getTopStroke());
        list.pseudoClassStateChanged(focused, list.isFocused());
        VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
        assertEquals(0, flow.getFirstVisibleCell().getIndex());
        double beforeDownPosition = flow.getPosition();
        press(list, KeyCode.DOWN);
        layout(stage);
        int afterDown = flow.getFirstVisibleCell().getIndex();
        assertTrue(flow.getPosition() > beforeDownPosition);
        press(list, KeyCode.PAGE_DOWN);
        layout(stage);
        int afterPageDown = flow.getFirstVisibleCell().getIndex();
        assertTrue(afterPageDown > afterDown + 1);
        press(list, KeyCode.PAGE_UP);
        layout(stage);
        assertTrue(flow.getFirstVisibleCell().getIndex() < afterPageDown);
        press(list, KeyCode.END);
        layout(stage);
        assertEquals(list.getItems().size() - 1, flow.getLastVisibleCell().getIndex());
        assertTrue(flow.getLastVisibleCell().getBoundsInParent().getMaxY() <= flow.getHeight() + 1);
        press(list, KeyCode.PAGE_DOWN);
        layout(stage);
        assertEquals(list.getItems().size() - 1, flow.getLastVisibleCell().getIndex());
        int beforeUp = flow.getFirstVisibleCell().getIndex();
        press(list, KeyCode.UP);
        layout(stage);
        assertTrue(flow.getFirstVisibleCell().getIndex() < beforeUp);
        press(list, KeyCode.HOME);
        layout(stage);
        assertEquals(0, flow.getFirstVisibleCell().getIndex());
        press(list, KeyCode.UP);
        assertEquals(0, flow.getFirstVisibleCell().getIndex());
        flow.fireEvent(new ScrollEvent(ScrollEvent.SCROLL, 5, 5, 5, 5,
                false, false, false, false, false, false, 0, -120, 0, -120,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0, ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null));
        layout(stage);
        assertTrue(flow.getPosition() > 0, "Mouse-wheel scrolling must remain available");
        ScrollBar bar = list.lookupAll(".scroll-bar").stream().map(ScrollBar.class::cast)
                .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL).findFirst().orElseThrow();
        assertTrue(bar.isVisible());
        bar.setValue(bar.getMax());
        layout(stage);
        assertEquals(list.getItems().size() - 1, flow.getLastVisibleCell().getIndex());
        click(flow.getFirstVisibleCell());
        assertSame(list, stage.getScene().getFocusOwner());
        assertNull(list.getSelectionModel());
        assertFalse(list.lookupAll(".list-cell").stream().map(ListCell.class::cast).anyMatch(ListCell::isSelected));
    }

    private static void assertNormalCardFont(Label label) {
        assertEquals(12, label.getFont().getSize(), label.getId());
        assertEquals(Font.font(label.getFont().getFamily(), FontWeight.NORMAL, 12), label.getFont(), label.getId());
    }

    private static void assertHeaders(Stage stage, int shownClasses, int totalClasses,
            int shownStudents, int totalStudents) {
        Label classHeader = (Label) stage.getScene().lookup("#classHeader");
        Label studentHeader = (Label) stage.getScene().lookup("#studentHeader");
        Label classCount = (Label) stage.getScene().lookup("#classCount");
        Label studentCount = (Label) stage.getScene().lookup("#studentCount");
        assertEquals("Classes", classHeader.getText());
        assertEquals("Students", studentHeader.getText());
        assertEquals(shownClasses + " shown / " + totalClasses + " total", classCount.getText());
        assertEquals(shownStudents + " shown / " + totalStudents + " total", studentCount.getText());
    }

    private static void assertHeaderLayout(Stage stage, String prefix) {
        Label title = (Label) stage.getScene().lookup("#" + prefix + "Header");
        Label count = (Label) stage.getScene().lookup("#" + prefix + "Count");
        Region row = (Region) title.getParent();
        assertSame(row, count.getParent());
        assertTrue(title.getBoundsInParent().getMaxX() < count.getBoundsInParent().getMinX());
        assertEquals(row.getWidth(), count.getBoundsInParent().getMaxX(), 1);
        assertTrue(count.getBoundsInParent().getMinY() >= 0);
        assertTrue(count.getBoundsInParent().getMaxY() <= row.getHeight() + 1);
        assertTrue(count.getStyleClass().contains("count-pill"));
        assertTrue(count.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius()
                >= count.getHeight() / 2);
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
        return createLogic(extraClassCount, 0);
    }

    private Logic createLogic(int extraClassCount, int extraStudentCount) {
        AddressBook book = new AddressBook();
        for (var student : List.of(ALICE, BENSON, DANIEL)) {
            book.addPerson(student);
        }
        for (int index = 0; index < extraStudentCount; index++) {
            book.addPerson(new PersonBuilder().withStudentNumber("SCROLL" + index)
                    .withName("Student " + index).withEmail("student" + index + "@example.com").build());
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

    private static void click(Node target) {
        for (var eventType : List.of(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED)) {
            target.fireEvent(new MouseEvent(eventType, 5, 5, 5, 5, MouseButton.PRIMARY,
                    1, false, false, false, false, eventType == MouseEvent.MOUSE_PRESSED,
                    false, false, true, false, true, null));
        }
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
