package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
            assertLabelTextExists(academicClassCard.getRoot(), "F10-2");
            assertLabelTextExists(academicClassCard.getRoot(), "Module: CS2103T");
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
