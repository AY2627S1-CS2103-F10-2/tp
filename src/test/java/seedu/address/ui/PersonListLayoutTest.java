package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Exercises variable-height student cards after virtualized cells are recycled and resized. */
public class PersonListLayoutTest {
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
    public void repeatedScrollingAndResizing_keepOptionalDetailsWithinTheirOwnCards() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            var people = FXCollections.observableArrayList(IntStream.range(0, 60)
                    .mapToObj(PersonListLayoutTest::createPerson).toList());
            PersonListPanel panel = new PersonListPanel(people, Optional::empty, List::of);
            panel.getRoot().getStyleClass().addAll("app-shell", "workspace-bottom");
            Stage stage = new Stage();
            Scene scene = new Scene(panel.getRoot(), 300, 440);
            scene.getStylesheets().add(getClass().getResource("/view/Workspace.css").toExternalForm());
            stage.setScene(scene);
            try {
                stage.show();
                settle(stage);
                ListView<?> list = (ListView<?>) panel.getRoot().lookup(".list-view");
                VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                for (double width : List.of(250.0, 430.5, 310.5, 560.0, 250.0)) {
                    stage.setWidth(width);
                    settle(stage);
                    for (int target : List.of(0, 12, 30, 59, 42, 17, 0)) {
                        list.scrollTo(target);
                        settle(stage);
                        int first = flow.getFirstVisibleCell().getIndex();
                        int last = flow.getLastVisibleCell().getIndex();
                        for (int index = first; index <= last; index++) {
                            assertCardLayout((ListCell<?>) flow.getVisibleCell(index), people.get(index));
                        }
                    }
                }
            } finally {
                stage.hide();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    @Test
    public void repeatedEditsAndScrolling_withAttendance_relayoutOptionalDetails() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            var people = FXCollections.observableArrayList(IntStream.range(0, 60)
                    .mapToObj(PersonListLayoutTest::createPerson).toList());
            AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
            List<Attendance> records = IntStream.range(0, people.size()).mapToObj(index -> {
                Attendance record = new Attendance(people.get(index), academicClass);
                if (index % 3 == 0) {
                    for (int day = 1; day <= 12; day++) {
                        record.addDate(LocalDate.of(2020, 1, day));
                    }
                }
                return record;
            }).toList();
            PersonListPanel panel = new PersonListPanel(people, () -> Optional.of(academicClass), () -> records);
            panel.getRoot().getStyleClass().addAll("app-shell", "workspace-bottom");
            Stage stage = new Stage();
            Scene scene = new Scene(panel.getRoot(), 300, 440);
            scene.getStylesheets().add(getClass().getResource("/view/Workspace.css").toExternalForm());
            stage.setScene(scene);
            try {
                stage.show();
                settle(stage);
                ListView<?> list = (ListView<?>) panel.getRoot().lookup(".list-view");
                for (double width : List.of(250.0, 560.0, 310.5)) {
                    stage.setWidth(width);
                    settle(stage);
                    for (int index : List.of(0, 17, 59)) {
                        list.scrollTo(index);
                        settle(stage);
                        assertVisibleCardsWithAttendance(list, people, records);
                        Person original = people.get(index);
                        for (boolean showDetails : List.of(false, true, false)) {
                            Person edited = new PersonBuilder(original)
                                    .withTelegram(showDetails ? "w".repeat(30) + String.format("%02d", index) : null)
                                    .withTags(showDetails ? new String[] {"zeta", "alpha", "student" + index,
                                        "aVeryLongTagThatMustWrapInsideThisStudentCard"} : new String[] {})
                                    .build();
                            // Replacing the displayed record exercises the production refresh listener.
                            people.set(index, edited);
                            list.scrollTo(index);
                            settle(stage);
                            VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
                            assertNotNull(flow.getVisibleCell(index), "The edited card must be checked while visible");
                            assertVisibleCardsWithAttendance(list, people, records);

                            // Recycle cells away from the edited record, then verify its details again.
                            list.scrollTo(index < 30 ? 59 : 0);
                            settle(stage);
                            assertVisibleCardsWithAttendance(list, people, records);
                            list.scrollTo(index);
                            settle(stage);
                            assertNotNull(flow.getVisibleCell(index));
                            assertVisibleCardsWithAttendance(list, people, records);
                        }
                    }
                }
            } finally {
                stage.hide();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    private static void assertVisibleCardsWithAttendance(ListView<?> list, List<Person> people,
            List<Attendance> records) {
        VirtualFlow<?> flow = (VirtualFlow<?>) list.lookup(".virtual-flow");
        int first = flow.getFirstVisibleCell().getIndex();
        int last = flow.getLastVisibleCell().getIndex();
        for (int index = first; index <= last; index++) {
            ListCell<?> cell = (ListCell<?>) flow.getVisibleCell(index);
            Person person = people.get(index);
            assertCardLayout(cell, person);
            Node card = cell.getGraphic();
            Label attendance = (Label) card.lookup("#attendance");
            assertTrue(attendance.isManaged());
            assertTrue(attendance.isVisible());
            List<String> dates = records.get(index).getAttendedDates().stream().map(Object::toString).toList();
            assertEquals(dates.isEmpty() ? "No attendance recorded" : String.join(", ", dates), attendance.getText());
            Node tags = card.lookup("#tags");
            Node previousGroup = tags.isManaged() ? tags : card.lookup("#details");
            String context = person.getStudentNumber().value + " at width " + cell.getWidth();
            assertTrue(previousGroup.localToScene(previousGroup.getBoundsInLocal()).getMaxY()
                    <= attendance.localToScene(attendance.getBoundsInLocal()).getMinY(),
                    context + " attendance overlaps contact details or tags");
            assertTrue(attendance.getHeight() + 1 >= attendance.prefHeight(attendance.getWidth()),
                    context + " wrapped attendance has insufficient height");
            assertTrue(attendance.localToScene(attendance.getBoundsInLocal()).getMaxX()
                    <= card.localToScene(card.getBoundsInLocal()).getMaxX() + 1,
                    context + " attendance extends outside its card");
        }
    }

    private static Person createPerson(int index) {
        boolean longDetails = index % 3 == 0;
        return new PersonBuilder().withStudentNumber("LAYOUT" + index)
                .withName(longDetails ? "Student With A Long Name Number " + index : "Student " + index)
                .withEmail(longDetails ? "a.very.long.student.email.address" + index + "@example.com"
                        : "student" + index + "@example.com")
                .withTelegram(index % 2 == 0 ? "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww" : null)
                .withTags(index % 4 == 0 ? new String[] {} : new String[] {
                    "tutorial", longDetails ? "aVeryLongTagThatMustWrapInsideThisStudentCard" : "project"})
                .build();
    }

    private static void assertCardLayout(ListCell<?> cell, Person person) {
        Node card = cell.getGraphic();
        String context = person.getStudentNumber().value + " at width " + cell.getWidth();
        assertEquals(person.getName().fullName, ((Label) card.lookup("#name")).getText(), context);
        assertEquals(person.getStudentNumber().value, ((Label) card.lookup("#studentNumber")).getText(), context);
        assertEquals(person.getEmail().value, ((Label) card.lookup("#email")).getText(), context);
        Label telegram = (Label) card.lookup("#telegram");
        assertEquals(person.getTelegram().isPresent(), telegram.isManaged(), context);
        assertEquals(person.getTelegram().isPresent(), telegram.isVisible(), context);
        person.getTelegram().ifPresent(value -> assertEquals("@" + value.value, telegram.getText(), context));
        FlowPane details = (FlowPane) card.lookup("#details");
        FlowPane tags = (FlowPane) card.lookup("#tags");
        assertEquals(!person.getTags().isEmpty(), tags.isManaged(), context);
        assertEquals(!person.getTags().isEmpty(), tags.isVisible(), context);
        assertEquals(person.getTags().stream().map(tag -> tag.tagName).sorted().toList(),
                tags.getChildren().stream().map(node -> ((Label) node).getText()).toList(), context);
        for (FlowPane group : List.of(details, tags)) {
            if (!group.isManaged()) {
                continue;
            }
            for (Node label : group.getChildren().filtered(Node::isManaged)) {
                assertTrue(label.getBoundsInParent().getMaxY() <= group.getHeight() + 1,
                        context + " " + group.getId() + " child extends below group: "
                                + label.getBoundsInParent() + " / " + group.getHeight());
                assertTrue(label.getBoundsInParent().getMaxX() <= group.getWidth() + 1, context);
                assertTrue(label.getBoundsInParent().getHeight() + 1 >= label.prefHeight(label.getLayoutBounds()
                        .getWidth()), context + " wrapped text has insufficient height");
            }
        }
        if (tags.isManaged()) {
            assertTrue(details.localToScene(details.getBoundsInLocal()).getMaxY()
                    <= tags.localToScene(tags.getBoundsInLocal()).getMinY(), context + " overlapping groups");
        }
        assertTrue(card.getBoundsInParent().getMaxY() <= cell.getHeight() + 1,
                context + " card exceeds allocated row height");
    }

    private static void settle(Stage stage) {
        Object frame = new Object();
        PauseTransition pause = new PauseTransition(Duration.millis(40));
        pause.setOnFinished(event -> Platform.exitNestedEventLoop(frame, null));
        pause.play();
        Platform.enterNestedEventLoop(frame);
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
    }
}
