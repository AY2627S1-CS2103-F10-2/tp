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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
            assertEquals("CS2103T ", ((Text) academicClassCard.getRoot().lookup("#moduleCode")).getText());
            assertEquals("F10-2", ((Text) academicClassCard.getRoot().lookup("#className")).getText());
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

            TextFlow heading = (TextFlow) card.getRoot().lookup("#classHeading");
            Text name = (Text) card.getRoot().lookup("#className");
            Text moduleCode = (Text) card.getRoot().lookup("#moduleCode");
            assertTrue(heading.getHeight() > name.getFont().getSize() * 2);
            assertTrue(heading.localToScene(heading.getBoundsInLocal()).getMaxX() <= container.getWidth());
            assertEquals("CS2103T ", moduleCode.getText());
            assertEquals(academicClass.getClassName().value, name.getText());
            Label membership = (Label) card.getRoot().lookup("#studentCount");
            assertTrue(membership.localToScene(0, 0).getY()
                    >= heading.localToScene(heading.getBoundsInLocal()).getMaxY());
            assertLabelTextExists(card.getRoot(), "123. ");
            assertLabelTextExists(card.getRoot(), "Students: 0");
        });
    }

    @Test
    public void heading_longClassName_wrapsAsCompactlyAsSingleLabel() {
        runOnFxThreadAndWait(() -> {
            AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"),
                    new ClassName("TUTORIAL-GROUP-WITH-A-LONG-NAME"));
            AcademicClassCard card = new AcademicClassCard(academicClass, 1);
            StackPane container = createContainer(card);
            layoutCard(container, 230);

            TextFlow heading = (TextFlow) card.getRoot().lookup("#classHeading");
            Text name = (Text) card.getRoot().lookup("#className");
            Label reference = new Label("CS2103 " + academicClass.getClassName().value);
            reference.setFont(name.getFont());
            reference.setWrapText(true);
            reference.setMinWidth(0);
            StackPane measurement = new StackPane(reference);
            new Scene(measurement, heading.getWidth(), 600);
            measurement.applyCss();
            measurement.layout();
            assertTrue(heading.getHeight() <= reference.prefHeight(heading.getWidth()) + 1,
                    "Colouring the module separately must not add heading rows");
            assertTrue(heading.getHeight() > name.getFont().getSize() * 1.5);
            assertHeadingVisible(card);
        });
    }

    @Test
    public void heading_resizeWithLongModuleCodes_wrapsAndRecoversCompactHeight() {
        runOnFxThreadAndWait(() -> {
            for (String code : new String[] {"CS2103", "W".repeat(32)}) {
                AcademicClass academicClass = new AcademicClass(new ModuleCode(code),
                        new ClassName("TUTORIAL-GROUP-WITH-A-VERY-LONG-NAME"));
                AcademicClassCard card = new AcademicClassCard(academicClass, 123);
                StackPane container = createContainer(card);
                layoutCard(container, 700);
                double wideHeight = card.getRoot().prefHeight(card.getRoot().getWidth());
                for (double width : new double[] {230, 180, 700, 180, 700}) {
                    layoutCard(container, width);
                    Text moduleCode = (Text) card.getRoot().lookup("#moduleCode");
                    Text name = (Text) card.getRoot().lookup("#className");
                    assertEquals(code + " ", moduleCode.getText());
                    assertEquals(academicClass.getClassName().value, name.getText());
                    assertHeadingVisible(card);
                    double height = card.getRoot().prefHeight(card.getRoot().getWidth());
                    if (width == 700) {
                        assertEquals(wideHeight, height, 1, "Widening must recover the compact card height");
                    } else {
                        assertTrue(height > wideHeight, "Narrow cards must grow to fit wrapped headings");
                    }
                    assertLabelTextExists(card.getRoot(), "123. ");
                    assertLabelTextExists(card.getRoot(), "Students: 0");
                }
            }
        });
    }

    private StackPane createContainer(AcademicClassCard card) {
        StackPane container = new StackPane(card.getRoot());
        container.getStyleClass().addAll("app-shell", "workspace-bottom");
        Scene scene = new Scene(container, 700, 600);
        scene.getStylesheets().add(getClass().getResource("/view/Workspace.css").toExternalForm());
        return container;
    }

    private static void layoutCard(StackPane container, double width) {
        container.resize(width, 600);
        container.applyCss();
        container.layout();
        container.layout();
    }

    private static void assertHeadingVisible(AcademicClassCard card) {
        TextFlow heading = (TextFlow) card.getRoot().lookup("#classHeading");
        for (Node part : heading.getChildren()) {
            var bounds = part.getBoundsInParent();
            assertTrue(bounds.getMinX() >= -1 && bounds.getMaxX() <= heading.getWidth() + 1);
            assertTrue(bounds.getMinY() >= -1 && bounds.getMaxY() <= heading.getHeight() + 1);
        }
        Label membership = (Label) card.getRoot().lookup("#studentCount");
        assertTrue(membership.localToScene(0, 0).getY()
                >= heading.localToScene(heading.getBoundsInLocal()).getMaxY());
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
