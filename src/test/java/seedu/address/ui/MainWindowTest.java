package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void setUpBeforeClass() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX toolkit has already been initialized.
        }
        Platform.setImplicitExit(false);
    }

    @Test
    public void restoreWindow_disconnectedMonitor_centersOnPrimaryScreen() {
        Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);
        assertEquals(new Rectangle2D(590, 240, 740, 600), MainWindow.getVisibleWindowBounds(
                new GuiSettings(740, 600, 2000, 100), List.of(primary), primary));
    }

    @Test
    public void restoreWindow_offScreenNegativeCoordinates_centersOnPrimaryScreen() {
        Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);
        for (int[] position : new int[][] {{-10000, 100}, {100, -10000}, {100, -20}}) {
            assertEquals(new Rectangle2D(590, 240, 740, 600), MainWindow.getVisibleWindowBounds(
                    new GuiSettings(740, 600, position[0], position[1]), List.of(primary), primary));
        }
    }

    @Test
    public void restoreWindow_connectedMonitorWithNegativeCoordinates_preservesPosition() {
        Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);
        Rectangle2D secondary = new Rectangle2D(-1920, 0, 1920, 1080);
        assertEquals(new Rectangle2D(-1800, 100, 740, 600), MainWindow.getVisibleWindowBounds(
                new GuiSettings(740, 600, -1800, 100), List.of(primary, secondary), primary));
    }

    @Test
    public void restoreWindow_oversizedWindow_fitsPrimaryScreen() {
        Rectangle2D primary = new Rectangle2D(0, 30, 1280, 720);
        assertEquals(primary, MainWindow.getVisibleWindowBounds(
                new GuiSettings(2560, 1440, 0, 0), List.of(primary), primary));
    }

    @Test
    public void restoreWindow_invalidDimensions_usesDefaults() {
        Rectangle2D screen = new Rectangle2D(0, 0, 1920, 1080);
        for (double invalid : new double[] {-1, 0, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(new Rectangle2D(100, 100, 740, 600), MainWindow.getVisibleWindowBounds(
                    new GuiSettings(invalid, invalid, 100, 100), List.of(screen), screen));
        }
    }

    @Test
    public void restoreWindow_smallScreen_shownStageFitsDespiteFxmlMinimums() {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, new LogicStub(), temporaryFolder.resolve("addressBook.json"));
            Rectangle2D screen = new Rectangle2D(0, 0, 400, 560);
            Rectangle2D bounds = MainWindow.getVisibleWindowBounds(
                    new GuiSettings(740, 600, 0, 0), List.of(screen), screen);
            try {
                MainWindow.applyWindowBounds(stage, bounds);
                stage.show();
                assertTrue(stage.getMinWidth() <= screen.getWidth());
                assertTrue(stage.getMinHeight() <= screen.getHeight());
                assertTrue(stage.getWidth() <= screen.getWidth());
                assertTrue(stage.getHeight() <= screen.getHeight());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void fillInnerParts_addsPanelsToPlaceholders() {
        runOnFxThreadAndWait(() -> {
            MainWindow mainWindow = new MainWindow(new Stage(), new LogicStub(),
                    temporaryFolder.resolve("addressBook.json"));

            mainWindow.fillInnerParts();

            assertNotNull(mainWindow.getPersonListPanel());
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

    private static class LogicStub implements Logic {

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return FXCollections.observableArrayList();
        }

        @Override
        public Optional<AcademicClass> getAttendanceDisplayClass() {
            return Optional.empty();
        }

        @Override
        public List<Attendance> getAttendanceList() {
            return List.of();
        }

        @Override
        public ObservableList<AcademicClass> getAcademicClassList() {
            return FXCollections.observableArrayList();
        }

        @Override
        public ObservableList<AcademicClass> getFilteredAcademicClassList() {
            return FXCollections.observableArrayList();
        }

        @Override
        public GuiSettings getGuiSettings() {
            return new GuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }
    }
}
