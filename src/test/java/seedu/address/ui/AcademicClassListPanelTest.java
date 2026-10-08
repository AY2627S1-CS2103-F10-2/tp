package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

public class AcademicClassListPanelTest {

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
    public void constructor_validAcademicClassList_loadsPanel() {
        runOnFxThreadAndWait(() -> {
            ObservableList<AcademicClass> academicClasses = FXCollections.observableArrayList(CS2103T_F10);
            AcademicClassListPanel academicClassListPanel = new AcademicClassListPanel(academicClasses);

            assertNotNull(academicClassListPanel.getRoot());
            ListView<?> academicClassListView = (ListView<?>) academicClassListPanel.getRoot().lookup(".list-view");
            assertNotNull(academicClassListView.getCellFactory().call(null));
        });
    }

    @Test
    public void updateItem_emptyAcademicClass_clearsCell() {
        runOnFxThreadAndWait(() -> {
            AcademicClassListPanel academicClassListPanel =
                    new AcademicClassListPanel(FXCollections.observableArrayList());
            AcademicClassListPanel.AcademicClassListViewCell cell =
                    academicClassListPanel.new AcademicClassListViewCell();

            cell.updateItem(null, true);

            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @Test
    public void updateItem_nonEmptyAcademicClass_displaysCard() {
        runOnFxThreadAndWait(() -> {
            AcademicClassListPanel academicClassListPanel =
                    new AcademicClassListPanel(FXCollections.observableArrayList());
            AcademicClassListPanel.AcademicClassListViewCell cell =
                    academicClassListPanel.new AcademicClassListViewCell();

            cell.updateItem(CS2103T_F10, false);

            assertNotNull(cell.getGraphic());
        });
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
