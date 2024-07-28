package dev.ikm.orchestration.provider.window.menu;

import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.fxutils.ResourceHelper;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.view.journal.JournalViewFactory;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.preferences.JournalWindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.search.SearchNodeFactory;
import dev.ikm.tinkar.common.alert.AlertStreams;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.fxutils.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.kview.fxutils.CssHelper.refreshPanes;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;
import static dev.ikm.orchestration.interfaces.CssService.CSS_LOCATION;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.SHORT_MIN_FORMATTER;

/**
 * Represents a task that is responsible for launching a new journal view window.
 */
public class NewJournalViewTask extends Task<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(NewJournalViewTask.class);

    private final PrefX prefX;
    private final EvtBus kViewEventBus;
    private final List<JournalController> journalControllersList;
    private final Module graphicsModule;

    /**
     * Initializes a new instance of the NewJournalViewTask class.
     *
     * @param prefX                  The PrefX object.
     * @param journalControllersList The list of JournalController objects.
     */
    public NewJournalViewTask(PrefX prefX, List<JournalController> journalControllersList) {
        this.prefX = prefX;
        this.journalControllersList = journalControllersList;
        this.kViewEventBus = EvtBusFactory.getInstance(EvtBus.class);
        this.graphicsModule = ModuleLayer.boot()
                .findModule("dev.ikm.komet.framework")
                // Optional<Module> at this point
                .orElseThrow();
    }

    /**
     * Performs a series of actions to create a new journal view window.
     * If the journal title is not set in the preferences, generates a new title based on the current timestamp and adds it to the list of saved windows.
     * Launches the journal view window with the specified settings.
     *
     * @return null
     * @throws Exception if an error occurs during the process
     */
    @Override
    protected Void call() throws Exception {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        if (this.prefX.getValue(JournalWindowSettings.JOURNAL_TITLE) == null) {
            String windowTitle = "Journal " + LocalDateTime.now().format(SHORT_MIN_FORMATTER);
            List<String> savedWindows = appPreferences.getList(WindowServiceKeys.SAVED_WINDOWS);

            int index = Character.getNumericValue('a'); // Lowercase a

            while (savedWindows.contains(windowTitle)) {
                windowTitle = "Journal " + LocalDateTime.now().format(SHORT_MIN_FORMATTER) + Character.toString(index++);
            }
            savedWindows.add(windowTitle);

            prefX.setValue(JournalWindowSettings.JOURNAL_TITLE, windowTitle);
        }
        launchJournalViewWindow(appPreferences, this.prefX);
        return null;
    }

    /**
     * When a user selects the menu option View/New Journal a new Stage Window is launched.
     * This method will load a navigation panel to be a publisher and windows will be connected (subscribed) to the activity stream.
     * @param journalWindowSettings if present will give the size and positioning of the journal window
     */
    private void launchJournalViewWindow(KometPreferences appPreferences, PrefX journalWindowSettings) {
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);

        Stage journalStageWindow = new Stage();
        journalStageWindow.setTitle(journalWindowSettings.getValue(JournalWindowSettings.JOURNAL_TITLE));
        FXMLLoader journalLoader = JournalViewFactory.createFXMLLoader();
        JournalController journalController;
        try {
            BorderPane journalBorderPane = journalLoader.load();
            journalController = journalLoader.getController();
            Scene sourceScene = new Scene(journalBorderPane, 1200, 800);

            // Add Komet.css and kview css
            sourceScene.getStylesheets().addAll(
                    graphicsModule.getClassLoader().getResource(CSS_LOCATION).toString(), 
                    CssHelper.defaultStyleSheet());

            // Attach a listener to provide a CSS refresher ability for each Journal window. Right double click settings button (gear)
            attachCSSRefresher(journalController.getSettingsToggleButton(), journalController.getJournalBorderPane());

            journalStageWindow.setScene(sourceScene);

            String journalName;
            if (journalWindowSettings != null) {
                // load journal specific window settings
                journalName = journalWindowSettings.getValue(JOURNAL_TITLE);
                journalStageWindow.setTitle(journalName);
                if (journalWindowSettings.getValue(JOURNAL_HEIGHT) != null) {
                    journalStageWindow.setHeight(journalWindowSettings.getValue(JOURNAL_HEIGHT));
                    journalStageWindow.setWidth(journalWindowSettings.getValue(JOURNAL_WIDTH));
                    journalStageWindow.setX(journalWindowSettings.getValue(JOURNAL_XPOS));
                    journalStageWindow.setY(journalWindowSettings.getValue(JOURNAL_YPOS));
                    journalController.recreateConceptWindows(journalWindowSettings);
                }else{
                    journalStageWindow.setMaximized(true);
                }
            }

            journalStageWindow.setOnCloseRequest(windowEvent -> {
                saveJournalWindowsToPreferences();
                // call shutdown method on the view
                journalController.shutdown();
                journalControllersList.remove(journalController);
                // enable Delete menu option
                journalWindowSettings.setValue(CAN_DELETE, true);
                kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        journalController.setWindowView(windowSettings.getView());

        // Launch windows window pane inside journal view
        journalStageWindow.setOnShown(windowEvent -> {
            //TODO: Refactor factory constructor calls below to use PluggableService (make constructors private)
            KometNodeFactory navigatorNodeFactory = new GraphNavigatorNodeFactory();
            KometNodeFactory searchNodeFactory = new SearchNodeFactory();

            journalController.launchKometFactoryNodes(
                    journalWindowSettings.getValue(JOURNAL_TITLE),
                    navigatorNodeFactory,
                    searchNodeFactory);
            journalController.loadNextGenSearchPanel();
        });
        // disable the delete menu option for a Journal Card.
        journalWindowSettings.setValue(CAN_DELETE, false);
        kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
        journalControllersList.add(journalController);
        journalStageWindow.show();
    }

    /**
     * Saves the journal windows to preferences.
     */
    private void saveJournalWindowsToPreferences() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalPreferences = appPreferences.node(JOURNAL_WINDOW);

        // Non launched journal windows should be preserved.
        List<String> journalSubWindowFoldersFromPref = journalPreferences.getList(JOURNAL_NAMES);

        // launched (journal Controllers List) will overwrite existing window preferences.
        List<String> journalSubWindowFolders = new ArrayList<>(journalControllersList.size());
        for(JournalController controller : journalControllersList) {
            String journalSubWindowPrefFolder = controller.generateJournalDirNameBasedOnTitle();
            journalSubWindowFolders.add(journalSubWindowPrefFolder);

            KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNAL_WINDOW +
                    File.separator + journalSubWindowPrefFolder);
            controller.saveConceptWindowPreferences(journalSubWindowPreferences);
            journalSubWindowPreferences.put(JOURNAL_TITLE, controller.getTitle());
            journalSubWindowPreferences.putDouble(JOURNAL_HEIGHT, controller.getHeight());
            journalSubWindowPreferences.putDouble(JOURNAL_WIDTH, controller.getWidth());
            journalSubWindowPreferences.putDouble(JOURNAL_XPOS, controller.getX());
            journalSubWindowPreferences.putDouble(JOURNAL_YPOS, controller.getY());
            journalSubWindowPreferences.put(JOURNAL_AUTHOR, LandingPageController.DEMO_AUTHOR);
            journalSubWindowPreferences.putLong(JOURNAL_LAST_EDIT, (LocalDateTime.now())
                    .atZone(ZoneId.systemDefault()).toEpochSecond());
            try {
                journalSubWindowPreferences.flush();
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }

        }

        // Make sure windows that are not summoned will not be deleted (not added to JOURNAL_NAMES)
        for (String x : journalSubWindowFolders){
            if (!journalSubWindowFoldersFromPref.contains(x)) {
                journalSubWindowFoldersFromPref.add(x);
            }
        }
        journalPreferences.putList(JOURNAL_NAMES, journalSubWindowFoldersFromPref);

        try {
            journalPreferences.flush();
            appPreferences.flush();
            appPreferences.sync();
        } catch (BackingStoreException e) {
            LOG.error("error writing journal window flag to preferences", e);
            AlertStreams.dispatchToRoot(e);
        }
    }

    /**
     * This attaches a listener for the right mouse double click to refresh CSS stylesheet files.
     * @param node The node the user will right mouse button double click
     * @param root The root Parent node to refresh CSS stylesheets.
     */
    private void attachCSSRefresher(Node node, Parent root) {
        // CSS refresher 'easter egg'. Right Click settings button to refresh Css Styling
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (mouseEvent.getClickCount() == 2 && mouseEvent.isSecondaryButtonDown()) {
                handleRefreshUserCss(root);
            }
        });
    }

    /**
     * Will refresh a parent root node and all children that have CSS stylesheets.
     * Komet.css and kview-opt2.css files are updated dynamically.
     * @param root Parent node to be traversed to refresh all stylesheets.
     */
    private void handleRefreshUserCss(Parent root) {

        try {
            // "Feature" to make css editing/testing easy in the dev environment. Komet css
            String currentDir = System.getProperty("user.dir").replace("/application", "/framework/src/main/resources");
            String kometCssSourcePath = currentDir + ResourceHelper.toAbsolutePath("komet.css", Icon.class);
            File kometCssSourceFile = new File(kometCssSourcePath);

            // kView CSS file
            String kViewCssPath = defaultStyleSheet().replace("target/classes", "src/main/resources");
            File kViewCssFile = new File(kViewCssPath.replace("file:", ""));

            LOG.info("File exists? %s komet css path = %s".formatted(kometCssSourceFile.exists(), kometCssSourceFile));
            LOG.info("File exists? %s kview css path = %s".formatted(kViewCssFile.exists(), kViewCssFile));

            // ensure both exist on the development environment path
            if (kometCssSourceFile.exists() && kViewCssFile.exists()) {
                Scene scene = root.getScene();

                // Apply Komet css
                scene.getStylesheets().clear();
                scene.getStylesheets().add(kometCssSourceFile.toURI().toURL().toString());

                // Recursively refresh any children using the kView css files.
                refreshPanes(root, kViewCssPath);

                LOG.info("       Updated komet.css: " + kometCssSourceFile.getAbsolutePath());
                LOG.info("Updated kview css file: " + kViewCssFile.getAbsolutePath());
            } else {
                LOG.info("File not found for komet.css: " + kometCssSourceFile.getAbsolutePath());
            }
        } catch (IOException e) {
            // TODO: Raise an alert
            e.printStackTrace();
            AlertStreams.dispatchToRoot(e);
        }

    }
}
