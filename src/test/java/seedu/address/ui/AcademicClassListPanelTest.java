package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollToEvent;
import seedu.address.model.ModelManager;
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
            assertEquals(Orientation.VERTICAL, academicClassListView.getOrientation());
            assertNull(academicClassListView.getSelectionModel());
            assertTrue(academicClassListView.isFocusTraversable());
        });
    }

    @Test
    public void scrollToClass_onlyDisplayedIndices_issueScrollRequests() {
        runOnFxThreadAndWait(() -> {
            ObservableList<AcademicClass> classes = FXCollections.observableArrayList(CS2103T_F10);
            AcademicClassListPanel panel = new AcademicClassListPanel(classes);
            Scene scene = new Scene(panel.getRoot(), 300, 200);
            assertNotNull(scene);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            ListView<?> listView = (ListView<?>) panel.getRoot().lookup(".list-view");
            AtomicInteger scrollRequests = new AtomicInteger();
            listView.addEventHandler(ScrollToEvent.scrollToTopIndex(), event -> scrollRequests.incrementAndGet());

            panel.scrollToClass(-1);
            panel.scrollToClass(classes.size());
            assertEquals(0, scrollRequests.get());
            panel.scrollToClass(0);
            assertEquals(1, scrollRequests.get());
            classes.clear();
            panel.scrollToClass(0);
            assertEquals(1, scrollRequests.get());
            assertNull(listView.getSelectionModel());
        });
    }

    @Test
    public void deleteAcademicClass_updatesBoundListView() {
        runOnFxThreadAndWait(() -> {
            ModelManager model = new ModelManager();
            AcademicClass remaining = new AcademicClass(new ModuleCode("ST2334"), new ClassName("T24"));
            model.addAcademicClass(CS2103T_F10);
            model.addAcademicClass(remaining);
            AcademicClassListPanel panel = new AcademicClassListPanel(model.getAcademicClassList());
            ListView<?> listView = (ListView<?>) panel.getRoot().lookup(".list-view");
            assertEquals(List.of(CS2103T_F10, remaining), listView.getItems());

            model.deleteAcademicClass(CS2103T_F10);

            assertEquals(List.of(remaining), listView.getItems());
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
