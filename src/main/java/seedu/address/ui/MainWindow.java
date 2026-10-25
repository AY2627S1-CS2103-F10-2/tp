package seedu.address.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.AddAttendanceCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteAttendanceCommand;
import seedu.address.logic.commands.EditClassCommand;
import seedu.address.logic.commands.ListByClassCommand;
import seedu.address.logic.commands.UnassignCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final double MIN_WINDOW_WIDTH = 720;
    private static final double MIN_WINDOW_HEIGHT = 600;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;
    private final Supplier<List<Rectangle2D>> screenBounds;
    private final Supplier<Rectangle2D> primaryScreenBounds;
    private final InvalidationListener screenChangeListener = observable -> updateWindowScreen();
    private Rectangle2D currentScreenBounds;
    private boolean applyingWindowBounds;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private AcademicClassListPanel academicClassListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    @FXML
    private Label classCount;

    @FXML
    private Label studentCount;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane academicClassListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        this(primaryStage, logic, dataFilePath, () ->
                Screen.getScreens().stream().map(Screen::getVisualBounds).toList(), () ->
                Screen.getPrimary().getVisualBounds());
    }

    /** Creates a window using the supplied monitor geometry. */
    MainWindow(Stage primaryStage, Logic logic, Path dataFilePath,
            Supplier<List<Rectangle2D>> screenBounds, Supplier<Rectangle2D> primaryScreenBounds) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        this.screenBounds = screenBounds;
        this.primaryScreenBounds = primaryScreenBounds;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());
        primaryStage.xProperty().addListener(screenChangeListener);
        primaryStage.yProperty().addListener(screenChangeListener);
        primaryStage.widthProperty().addListener(screenChangeListener);
        primaryStage.heightProperty().addListener(screenChangeListener);
        primaryStage.showingProperty().addListener(screenChangeListener);
        primaryStage.iconifiedProperty().addListener((observable, wasIconified, isIconified) -> {
            if (!isIconified) {
                // Native window managers can restore saved bounds after the property changes.
                Platform.runLater(() -> updateWindowScreen(true));
            }
        });
        Screen.getScreens().addListener(new WeakInvalidationListener(screenChangeListener));

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList(), logic::getAttendanceDisplayClass,
                logic::getAttendanceList);
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        academicClassListPanel = new AcademicClassListPanel(logic.getFilteredAcademicClassList());
        academicClassListPanelPlaceholder.getChildren().add(academicClassListPanel.getRoot());

        classCount.textProperty().bind(Bindings.createStringBinding(() ->
                logic.getFilteredAcademicClassList().size() + " shown / "
                        + logic.getAcademicClassList().size() + " total",
                logic.getFilteredAcademicClassList(), logic.getAcademicClassList()));
        studentCount.textProperty().bind(Bindings.createStringBinding(() ->
                logic.getFilteredPersonList().size() + " shown / "
                        + logic.getPersonList().size() + " total",
                logic.getFilteredPersonList(), logic.getPersonList()));

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        List<Rectangle2D> screens = screenBounds.get();
        Rectangle2D primaryScreen = primaryScreenBounds.get();
        Rectangle2D bounds = getVisibleWindowBounds(guiSettings, screens, primaryScreen);
        currentScreenBounds = getWindowScreen(bounds, screens, primaryScreen);
        applyWindowBounds(primaryStage, bounds, currentScreenBounds);
    }

    /** Fits the window and its normal minimum dimensions inside the monitor's usable bounds. */
    static void applyWindowBounds(Stage stage, Rectangle2D bounds, Rectangle2D usableBounds) {
        Rectangle2D fitted = fitWindowBounds(bounds, usableBounds);
        stage.setMinWidth(Math.min(MIN_WINDOW_WIDTH, usableBounds.getWidth()));
        stage.setMinHeight(Math.min(MIN_WINDOW_HEIGHT, usableBounds.getHeight()));
        stage.setWidth(fitted.getWidth());
        stage.setHeight(fitted.getHeight());
        stage.setX(fitted.getMinX());
        stage.setY(fitted.getMinY());
    }

    /** Restores saved bounds on their monitor, or centers on the primary screen if disconnected. */
    static Rectangle2D getVisibleWindowBounds(GuiSettings settings, List<Rectangle2D> screens,
            Rectangle2D primaryScreen) {
        double width = settings.getWindowWidth();
        double height = settings.getWindowHeight();
        GuiSettings defaults = new GuiSettings();
        width = Double.isFinite(width) && width > 0 ? width : defaults.getWindowWidth();
        height = Double.isFinite(height) && height > 0 ? height : defaults.getWindowHeight();
        if (settings.getWindowCoordinates() != null) {
            Rectangle2D savedBounds = new Rectangle2D(settings.getWindowCoordinates().getX(),
                    settings.getWindowCoordinates().getY(), width, height);
            Rectangle2D screen = getWindowScreen(savedBounds, screens, primaryScreen);
            if (intersectionArea(savedBounds, screen) > 0) {
                return fitWindowBounds(savedBounds, screen);
            }
        }
        width = Math.min(primaryScreen.getWidth(), Math.max(MIN_WINDOW_WIDTH, width));
        height = Math.min(primaryScreen.getHeight(), Math.max(MIN_WINDOW_HEIGHT, height));
        return new Rectangle2D(primaryScreen.getMinX() + (primaryScreen.getWidth() - width) / 2,
                primaryScreen.getMinY() + (primaryScreen.getHeight() - height) / 2, width, height);
    }

    /** Updates limits only on monitor changes so normal dragging and resizing remain unrestricted. */
    private void updateWindowScreen() {
        updateWindowScreen(false);
    }

    private void updateWindowScreen(boolean fitAfterRestore) {
        if (applyingWindowBounds || !primaryStage.isShowing() || primaryStage.isIconified()) {
            return;
        }
        Rectangle2D bounds = new Rectangle2D(primaryStage.getX(), primaryStage.getY(),
                primaryStage.getWidth(), primaryStage.getHeight());
        Rectangle2D screen = getWindowScreen(bounds, screenBounds.get(), primaryScreenBounds.get());
        if (fitAfterRestore || !screen.equals(currentScreenBounds)) {
            applyingWindowBounds = true;
            try {
                currentScreenBounds = screen;
                applyWindowBounds(primaryStage, bounds, screen);
            } finally {
                applyingWindowBounds = false;
            }
        }
    }

    private static Rectangle2D fitWindowBounds(Rectangle2D bounds, Rectangle2D screen) {
        double width = Math.min(screen.getWidth(), Math.max(MIN_WINDOW_WIDTH, bounds.getWidth()));
        double height = Math.min(screen.getHeight(), Math.max(MIN_WINDOW_HEIGHT, bounds.getHeight()));
        double x = Math.max(screen.getMinX(), Math.min(bounds.getMinX(), screen.getMaxX() - width));
        double y = Math.max(screen.getMinY(), Math.min(bounds.getMinY(), screen.getMaxY() - height));
        return new Rectangle2D(x, y, width, height);
    }

    private static Rectangle2D getWindowScreen(Rectangle2D bounds, List<Rectangle2D> screens,
            Rectangle2D primaryScreen) {
        Rectangle2D selected = primaryScreen;
        double largestArea = 0;
        for (Rectangle2D screen : screens) {
            double area = intersectionArea(bounds, screen);
            if (area > largestArea) {
                selected = screen;
                largestArea = area;
            }
        }
        return selected;
    }

    private static double intersectionArea(Rectangle2D window, Rectangle2D screen) {
        double width = Math.max(0, Math.min(window.getMaxX(), screen.getMaxX())
                - Math.max(window.getMinX(), screen.getMinX()));
        double height = Math.max(0, Math.min(window.getMaxY(), screen.getMaxY())
                - Math.max(window.getMinY(), screen.getMinY()));
        return width * height;
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            OptionalInt classPosition = getCommandClassPosition(commandText);
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            classPosition.ifPresent(position -> academicClassListPanel.scrollToClass(
                    logic.getFilteredAcademicClassList().indexOf(logic.getAcademicClassList().get(position))));

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

    /** Resolves a class index before execution, since commands can change the displayed class list. */
    private OptionalInt getCommandClassPosition(String commandText) {
        String[] tokens = commandText.strip().split("\\s+");
        int indexToken = switch (tokens[0]) {
            case ListByClassCommand.COMMAND_WORD, EditClassCommand.COMMAND_WORD -> 1;
            case AddAttendanceCommand.COMMAND_WORD, DeleteAttendanceCommand.COMMAND_WORD,
                    UnassignCommand.COMMAND_WORD -> 2;
            default -> -1;
        };
        if (indexToken < 0 || tokens.length <= indexToken
                || !StringUtil.isNonZeroUnsignedInteger(tokens[indexToken])) {
            return OptionalInt.empty();
        }
        int displayedIndex = Integer.parseInt(tokens[indexToken]) - 1;
        if (displayedIndex >= logic.getFilteredAcademicClassList().size()) {
            return OptionalInt.empty();
        }
        // Index-based class commands preserve the class collection's order, including edits.
        return OptionalInt.of(logic.getAcademicClassList().indexOf(
                logic.getFilteredAcademicClassList().get(displayedIndex)));
    }
}
