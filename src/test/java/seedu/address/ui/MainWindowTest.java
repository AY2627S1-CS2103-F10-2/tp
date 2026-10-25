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
        for (int[] position : new int[][] {{-10000, 100}, {100, -10000}}) {
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
            Rectangle2D screen = new Rectangle2D(0, 0, 400, 560);
            new MainWindow(stage, new LogicStub(), temporaryFolder.resolve("addressBook.json"), () ->
                    List.of(screen), () -> screen);
            Rectangle2D bounds = MainWindow.getVisibleWindowBounds(
                    new GuiSettings(740, 600, 0, 0), List.of(screen), screen);
            assertEquals(screen, bounds);
            try {
                stage.show();
                assertEquals(400, stage.getMinWidth());
                assertEquals(560, stage.getMinHeight());
                assertTrue(stage.getWidth() <= screen.getWidth());
                assertTrue(stage.getHeight() <= screen.getHeight());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void restoreWindow_smallerSavedDimensions_expandsBeforeCheckingPosition() {
        Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);
        assertEquals(new Rectangle2D(100, 100, 720, 600), MainWindow.getVisibleWindowBounds(
                new GuiSettings(450, 500, 100, 100), List.of(primary), primary));
        assertEquals(new Rectangle2D(1200, 480, 720, 600), MainWindow.getVisibleWindowBounds(
                new GuiSettings(450, 500, 1470, 500), List.of(primary), primary));
    }

    @Test
    public void restoreWindow_scaledScreen_fitsUsableBoundsIncludingTaskbarOffsets() {
        Rectangle2D screen = new Rectangle2D(0, 20, 1280 / 1.5, 720 / 1.5 - 50);
        for (GuiSettings saved : List.of(new GuiSettings(), new GuiSettings(1920, 1080, 100, 100),
                new GuiSettings(450, 300, 800, 400), new GuiSettings(740, 600, 3000, 100))) {
            Rectangle2D bounds = MainWindow.getVisibleWindowBounds(saved, List.of(screen), screen);
            assertTrue(screen.contains(bounds));
            assertEquals(screen.getHeight(), bounds.getHeight());
            assertTrue(bounds.getWidth() >= 720);
        }
    }

    @Test
    public void restoreWindow_oversizedOnSmallerSecondary_keepsSelectedMonitor() {
        Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);
        Rectangle2D secondary = new Rectangle2D(-854, 20, 854, 430);
        assertEquals(new Rectangle2D(-854, 20, 740, 430), MainWindow.getVisibleWindowBounds(
                new GuiSettings(740, 600, -900, -20), List.of(primary, secondary), primary));
        assertEquals(new Rectangle2D(100, 0, 740, 600), MainWindow.getVisibleWindowBounds(
                new GuiSettings(740, 600, 100, -20), List.of(primary), primary));
    }

    @Test
    public void applyWindowBounds_oversizedAndOffScreen_clampsDimensionsAndPosition() {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            Rectangle2D screen = new Rectangle2D(-854, 20, 854, 430);
            MainWindow.applyWindowBounds(stage, new Rectangle2D(-1000, -50, 2000, 1200), screen);
            assertEquals(720, stage.getMinWidth());
            assertEquals(430, stage.getMinHeight());
            assertEquals(screen.getWidth(), stage.getWidth());
            assertEquals(screen.getHeight(), stage.getHeight());
            assertEquals(screen.getMinX(), stage.getX());
            assertEquals(screen.getMinY(), stage.getY());
            MainWindow.applyWindowBounds(stage, new Rectangle2D(-10, 440, 450, 300), screen);
            assertEquals(720, stage.getWidth());
            assertEquals(430, stage.getHeight());
            assertEquals(-720, stage.getX());
            assertEquals(20, stage.getY());
        });
    }

    @Test
    public void moveWindow_betweenMonitors_updatesLimitsAndFitsWindow() {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);
            Rectangle2D secondary = new Rectangle2D(1920, 20, 854, 430);
            new MainWindow(stage, new LogicStub(), temporaryFolder.resolve("addressBook.json"), () ->
                    List.of(primary, secondary), () -> primary);
            try {
                stage.show();
                assertEquals(600, stage.getMinHeight());
                stage.setX(2000);
                assertEquals(720, stage.getMinWidth());
                assertEquals(430, stage.getMinHeight());
                assertTrue(secondary.contains(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
                stage.setX(100);
                assertEquals(720, stage.getMinWidth());
                assertEquals(600, stage.getMinHeight());
                assertTrue(primary.contains(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
                stage.setX(200);
                assertEquals(200, stage.getX(), "Normal dragging within a monitor must retain its position");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void restoreWindow_monitorChangesWhileHidden_updatesLimitsWhenShownAgain() {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            Rectangle2D large = new Rectangle2D(50, 50, 1920, 1080);
            Rectangle2D small = new Rectangle2D(50, 50, 854, 430);
            AtomicReference<Rectangle2D> screen = new AtomicReference<>(large);
            new MainWindow(stage, new LogicStub(), temporaryFolder.resolve("addressBook.json"), () ->
                    List.of(screen.get()), screen::get);
            try {
                stage.show();
                assertEquals(600, stage.getMinHeight());
                stage.hide();
                screen.set(small);
                stage.show();
                assertEquals(430, stage.getMinHeight());
                assertTrue(small.contains(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void restoreWindow_monitorChangesWhileMinimized_updatesLimitsWhenRestored() {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            Rectangle2D large = new Rectangle2D(50, 50, 1920, 1080);
            Rectangle2D small = new Rectangle2D(50, 50, 854, 430);
            AtomicReference<Rectangle2D> screen = new AtomicReference<>(large);
            LogicStub logic = new LogicStub() {
                @Override
                public GuiSettings getGuiSettings() {
                    return new GuiSettings(740, 600, 100, 50);
                }
            };
            new MainWindow(stage, logic, temporaryFolder.resolve("addressBook.json"), () ->
                    List.of(screen.get()), screen::get);
            try {
                // Set logical window state before peer creation to avoid native minimize animations.
                stage.setIconified(true);
                screen.set(small);
                assertEquals(600, stage.getMinHeight(), "Defer resizing while the window is minimized");
                stage.setIconified(false);
                stage.show();
                // Reproduce a native restore reporting its saved size after deiconification.
                stage.setHeight(600);
                Object restore = new Object();
                Platform.runLater(() -> Platform.exitNestedEventLoop(restore, null));
                Platform.enterNestedEventLoop(restore);
                assertEquals(430, stage.getMinHeight());
                assertTrue(small.contains(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void restoreWindow_smallerSavedStage_enforcesMinimumDimensions() {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            LogicStub logic = new LogicStub() {
                @Override
                public GuiSettings getGuiSettings() {
                    return new GuiSettings(450, 500, 0, 0);
                }
            };
            new MainWindow(stage, logic, temporaryFolder.resolve("addressBook.json"));
            try {
                stage.show();
                assertEquals(720, stage.getMinWidth());
                assertEquals(600, stage.getMinHeight());
                assertTrue(stage.getWidth() >= 720);
                assertTrue(stage.getHeight() >= 600);
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
        public ObservableList<Person> getPersonList() {
            return FXCollections.observableArrayList();
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
