package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

public class AcademicClassCardTest {

    private static final AcademicClass CS2103T_F10 =
            new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));

    @BeforeAll
    public static void setUpBeforeClass() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX toolkit has already been initialized.
        }
    }

    @Test
    public void constructor_validAcademicClass_displaysAcademicClassDetails() {
        runOnFxThreadAndWait(() -> {
            AcademicClassCard academicClassCard = new AcademicClassCard(CS2103T_F10, 1);

            assertNotNull(academicClassCard.getRoot());
            assertLabelTextExists(academicClassCard.getRoot(), "1. ");
            assertLabelTextExists(academicClassCard.getRoot(), "CS2103T");
            assertLabelTextExists(academicClassCard.getRoot(), "F10-2");
            assertLabelTextExists(academicClassCard.getRoot(), "Students: 0");
        });
    }

    @Test
    public void longClassName_narrowCard_wrapsWithoutHidingIndexOrMembership() {
        runOnFxThreadAndWait(() -> {
            AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"),
                    new ClassName("TUTORIAL-GROUP-WITH-A-VERY-LONG-NAME"));
            AcademicClassCard card = new AcademicClassCard(academicClass, 123);
            StackPane container = new StackPane(card.getRoot());
            container.getStyleClass().addAll("app-shell", "workspace-bottom");
            Scene scene = new Scene(container, 180, 400);
            scene.getStylesheets().add(getClass().getResource("/view/Workspace.css").toExternalForm());
            container.applyCss();
            container.layout();
            container.layout();

            FlowPane heading = (FlowPane) card.getRoot().lookup("#classHeading");
            Label name = (Label) card.getRoot().lookup("#className");
            Label moduleCode = (Label) card.getRoot().lookup("#moduleCode");
            assertTrue(heading.getHeight() > name.getFont().getSize() * 2);
            assertTrue(heading.localToScene(heading.getBoundsInLocal()).getMaxX() <= container.getWidth());
            assertEquals("CS2103T", moduleCode.getText());
            assertEquals(academicClass.getClassName().value, ((Text) name.lookup(".text")).getText());
            Label membership = (Label) card.getRoot().lookup("#studentCount");
            assertTrue(membership.localToScene(0, 0).getY()
                    >= heading.localToScene(heading.getBoundsInLocal()).getMaxY());
            assertLabelTextExists(card.getRoot(), "123. ");
            assertLabelTextExists(card.getRoot(), "Students: 0");
        });
    }

    @Test
    public void studentCount_membershipChanges_updatesExistingCard() {
        runOnFxThreadAndWait(() -> {
            AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
            academicClass.assignStudent(ALICE);
            AcademicClassCard card = new AcademicClassCard(academicClass, 1);
            assertLabelTextExists(card.getRoot(), "Students: 1");

            academicClass.assignStudent(BENSON);
            assertLabelTextExists(card.getRoot(), "Students: 2");
            academicClass.assignStudent(BENSON);
            assertLabelTextExists(card.getRoot(), "Students: 2");
            academicClass.unassignStudent(ALICE);
            assertLabelTextExists(card.getRoot(), "Students: 1");
            academicClass.unassignStudent(ALICE);
            assertLabelTextExists(card.getRoot(), "Students: 1");
            academicClass.unassignStudent(BENSON);
            assertLabelTextExists(card.getRoot(), "Students: 0");
        });
    }

    private void assertLabelTextExists(Node root, String expectedText) {
        long numberOfMatchingLabels = root.lookupAll(".label").stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .filter(label -> expectedText.equals(label.getText()))
                .count();
        assertEquals(1, numberOfMatchingLabels);
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
            latch.await();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        if (thrown.get() != null) {
            throw new AssertionError(thrown.get());
        }
    }
}
