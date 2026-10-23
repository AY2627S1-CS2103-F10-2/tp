package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void setUpBeforeClass() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX toolkit has already been initialized.
        }
    }

    @Test
    public void constructor_withTelegram_displaysStudentDetailsAndSortedTags() {
        Person person = new PersonBuilder().withStudentNumber("a1234567x").withName("Alice Tan")
                .withEmail("alice@example.com").withTelegram("@alice_tan").withTags("tutorial", "project").build();
        runOnFxThreadAndWait(() -> {
            PersonCard card = new PersonCard(person, 2);

            assertEquals("2. ", getLabel(card, "id").getText());
            assertEquals("Alice Tan", getLabel(card, "name").getText());
            assertEquals("A1234567X", getLabel(card, "studentNumber").getText());
            assertEquals("alice@example.com", getLabel(card, "email").getText());
            Label telegram = getLabel(card, "telegram");
            assertEquals("@alice_tan", telegram.getText());
            assertTrue(telegram.isVisible());
            assertTrue(telegram.isManaged());
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertEquals(List.of("project", "tutorial"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
        });
    }

    @Test
    public void longDetails_narrowCard_wrapNamesContactsAndTags() {
        Person person = new PersonBuilder().withName("Alice Pauline With A Very Long Student Name")
                .withEmail("alice.pauline.with.a.long.email.address@example.com")
                .withTelegram("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww")
                .withTags("aVeryLongTagThatMustWrapWithinTheStudentCard", "tutorial").build();
        runOnFxThreadAndWait(() -> {
            PersonCard card = new PersonCard(person, 123);
            StackPane container = new StackPane(card.getRoot());
            container.getStyleClass().addAll("app-shell", "workspace-bottom");
            Scene scene = new Scene(container, 230, 500);
            scene.getStylesheets().add(getClass().getResource("/view/Workspace.css").toExternalForm());
            container.applyCss();
            container.layout();
            container.layout();

            for (String id : List.of("name", "email", "telegram")) {
                Label label = getLabel(card, id);
                assertTrue(label.isWrapText());
                assertTrue(label.getHeight() > label.getFont().getSize() * 1.5,
                        id + ": " + label.getWidth() + " x " + label.getHeight());
                assertTrue(label.localToScene(label.getBoundsInLocal()).getMaxX() <= container.getWidth(), id);
                assertEquals(label.getText(), ((Text) label.lookup(".text")).getText());
            }
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            for (var tag : tags.getChildren()) {
                assertTrue(tag.localToScene(tag.getBoundsInLocal()).getMaxX() <= container.getWidth());
            }
            assertEquals("123. ", getLabel(card, "id").getText());
            assertFalse(getLabel(card, "attendance").isManaged());
        });
    }

    @Test
    public void constructor_withoutTelegram_hidesTelegramAndRemovesItsLayoutSpace() {
        Person person = new PersonBuilder().build();
        runOnFxThreadAndWait(() -> {
            PersonCard card = new PersonCard(person, 1);

            Label telegram = getLabel(card, "telegram");
            assertFalse(telegram.isVisible());
            assertFalse(telegram.isManaged());
            assertEquals(person.getStudentNumber().value, getLabel(card, "studentNumber").getText());
            assertEquals(person.getEmail().value, getLabel(card, "email").getText());
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertTrue(tags.getChildren().isEmpty());
        });
    }

    @Test
    public void constructor_withAttendance_displaysAttendanceDates() {
        Person person = new PersonBuilder().build();
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"),
                new ClassName("F10-2"));
        Attendance attendance = new Attendance(person, academicClass);
        attendance.addDate(LocalDate.of(2020, 1, 1));
        attendance.addDate(LocalDate.of(2020, 1, 2));

        runOnFxThreadAndWait(() -> {
            PersonCard card = new PersonCard(person, 1, academicClass, List.of(attendance));

            Label attendanceLabel = getLabel(card, "attendance");
            assertEquals("2020-01-01, 2020-01-02", attendanceLabel.getText());
            assertTrue(attendanceLabel.getStyleClass().contains("cell_small_label"));
            assertTrue(attendanceLabel.isWrapText());
            assertTrue(attendanceLabel.isVisible());
            assertTrue(attendanceLabel.isManaged());
        });
    }

    @Test
    public void constructor_withoutAttendance_displaysEmptyAttendanceMessage() {
        Person person = new PersonBuilder().build();
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"),
                new ClassName("F10-2"));
        runOnFxThreadAndWait(() -> {
            PersonCard card = new PersonCard(person, 1, academicClass, List.of());

            assertEquals("No attendance recorded", getLabel(card, "attendance").getText());
        });
    }

    @Test
    public void constructor_withNonMatchingAttendance_displaysEmptyAttendanceMessage() {
        Person person = new PersonBuilder().build();
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"),
                new ClassName("F10-2"));
        Attendance differentStudentAttendance = new Attendance(new PersonBuilder().withName("Other Student")
                .withStudentNumber("A9999999Z").build(), academicClass);
        differentStudentAttendance.addDate(LocalDate.of(2020, 1, 1));
        AcademicClass differentClass = new AcademicClass(new ModuleCode("CS2103"),
                new ClassName("F11-2"));
        Attendance differentClassAttendance = new Attendance(person, differentClass);
        differentClassAttendance.addDate(LocalDate.of(2020, 1, 1));

        runOnFxThreadAndWait(() -> {
            PersonCard card = new PersonCard(person, 1, academicClass,
                    List.of(differentStudentAttendance, differentClassAttendance));

            assertEquals("No attendance recorded", getLabel(card, "attendance").getText());
        });
    }

    private static Label getLabel(PersonCard card, String id) {
        return (Label) card.getRoot().lookup("#" + id);
    }

    private static void runOnFxThreadAndWait(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                thrown.set(throwable);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX action timed out.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        if (thrown.get() != null) {
            throw new AssertionError(thrown.get());
        }
    }
}
