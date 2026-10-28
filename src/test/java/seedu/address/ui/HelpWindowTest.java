package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.stage.Stage;

public class HelpWindowTest {

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
    public void focus_minimizedWindow_restoresWindow() throws Exception {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            HelpWindow helpWindow = new HelpWindow(stage);
            try {
                helpWindow.show();
                stage.setIconified(true);
                assertTrue(helpWindow.isShowing());
                assertTrue(stage.isIconified());

                helpWindow.focus();

                assertTrue(helpWindow.isShowing());
                assertFalse(stage.isIconified());
            } finally {
                helpWindow.hide();
            }
        });
    }

    @Test
    public void focus_visibleWindow_keepsWindowOpen() throws Exception {
        runOnFxThreadAndWait(() -> {
            HelpWindow helpWindow = new HelpWindow();
            try {
                helpWindow.show();
                helpWindow.focus();
                helpWindow.focus();

                assertTrue(helpWindow.isShowing());
                assertFalse(helpWindow.getRoot().isIconified());
            } finally {
                helpWindow.hide();
            }
        });
    }

    @Test
    public void show_closedWindow_reopensWindow() throws Exception {
        runOnFxThreadAndWait(() -> {
            HelpWindow helpWindow = new HelpWindow();
            try {
                helpWindow.show();
                helpWindow.getRoot().close();
                assertFalse(helpWindow.isShowing());

                helpWindow.show();

                assertTrue(helpWindow.isShowing());
                assertFalse(helpWindow.getRoot().isIconified());
            } finally {
                helpWindow.hide();
            }
        });
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
