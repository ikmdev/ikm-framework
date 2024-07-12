package dev.ikm.orchestration.provider;

import dev.ikm.komet.details.DetailsNodeFactory;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.tabs.DetachableTab;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.KometStageController;
import dev.ikm.komet.framework.window.MainWindowRecord;
import dev.ikm.komet.framework.window.WindowComponent;
import dev.ikm.komet.list.ListNodeFactory;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.navigator.pattern.PatternNavigatorFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.progress.CompletionNodeFactory;
import dev.ikm.komet.progress.ProgressNodeFactory;
import dev.ikm.komet.table.TableNodeFactory;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.binary.Encodable;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.ikm.komet.search.SearchNodeFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.framework.KometNodeFactory.KOMET_NODES;
import static dev.ikm.komet.framework.window.WindowSettings.Keys.*;
import static dev.ikm.orchestration.interfaces.CssService.CSS_LOCATION;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.SHORT_MIN_FORMATTER;

public class NewClassicKometWindowTask extends Task<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(NewClassicKometWindowTask.class);


    @Override
    protected Void call() throws Exception {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();

        Stage stage = new Stage();

        List<String> savedWindows = appPreferences.getList(WindowServiceKeys.SAVED_WINDOWS);

        String windowTitle = "Komet " + LocalDateTime.now().format(SHORT_MIN_FORMATTER);

        int index = 97;
        while (savedWindows.contains(windowTitle)) {
            windowTitle = "Komet " + LocalDateTime.now().format(SHORT_MIN_FORMATTER) + Character.toString(index++);
        }
        stage.setTitle(windowTitle);
        savedWindows.add(windowTitle);
        appPreferences.putList(WindowServiceKeys.SAVED_WINDOWS, savedWindows);
        KometPreferences windowPreferences = appPreferences.node(windowTitle);

        loadFromPreferences(stage, windowPreferences);
        return null;
    }

    public static void loadFromPreferences(Stage stage, KometPreferences windowPreferences) throws IOException, BackingStoreException {
        Module graphicsModule = ModuleLayer.boot()
                .findModule("dev.ikm.komet.framework")
                // Optional<Module> at this point
                .orElseThrow();
        FXMLLoader kometStageLoader = new FXMLLoader(MainWindowRecord.class.getResource("KometStageScene.fxml"));
        // MultiParentGraphViewController
        BorderPane kometRoot = kometStageLoader.load();
        MainWindowRecord mainWindowRecord = new MainWindowRecord(kometStageLoader.getController(), kometRoot);

        KometStageController controller = mainWindowRecord.controller();

        //Loading/setting the Komet screen
        Scene kometScene = new Scene(kometRoot, 1800, 1024);
        kometScene.getStylesheets()
                .add(graphicsModule.getClassLoader().getResource(CSS_LOCATION).toString());

        generateWindowMenu(kometRoot);

        stage.setScene(kometScene);

        boolean windowInitialized = windowPreferences.getBoolean(KometStageController.WindowKeys.WINDOW_INITIALIZED, false);
        controller.setup(windowPreferences);

        if (!windowInitialized) {
            controller.setLeftTabs(makeDefaultLeftTabs(controller.windowView()), 0);
            controller.setCenterTabs(makeDefaultCenterTabs(controller.windowView()), 0);
            controller.setRightTabs(makeDefaultRightTabs(controller.windowView()), 1);
            windowPreferences.putBoolean(KometStageController.WindowKeys.WINDOW_INITIALIZED, true);
        } else {
            // Restore nodes from preferences.
            windowPreferences.get(LEFT_TAB_PREFERENCES).ifPresent(leftTabPreferencesName -> {
                restoreTab(windowPreferences, leftTabPreferencesName, controller.windowView(), node -> controller.leftBorderPaneSetCenter(node));
            });
            windowPreferences.get(CENTER_TAB_PREFERENCES).ifPresent(centerTabPreferencesName -> {
                restoreTab(windowPreferences, centerTabPreferencesName, controller.windowView(), node -> controller.centerBorderPaneSetCenter(node));
            });
            windowPreferences.get(RIGHT_TAB_PREFERENCES).ifPresent(rightTabPreferencesName -> {
                restoreTab(windowPreferences, rightTabPreferencesName, controller.windowView(), node -> controller.rightBorderPaneSetCenter(node));
            });
        }

        //Setting X and Y coordinates for location of the Komet stage
        stage.setX(controller.windowSettings().xLocationProperty().get());
        stage.setY(controller.windowSettings().yLocationProperty().get());
        stage.setHeight(controller.windowSettings().heightProperty().get());
        stage.setWidth(controller.windowSettings().widthProperty().get());
        finishSetup(stage, windowPreferences);
    }

    private static void finishSetup(Stage stage, KometPreferences windowPreferences) {
        try {
            stage.show();
            windowPreferences.parent().sync();
            stage.toFront();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private static void restoreTab(KometPreferences windowPreferences, String tabPreferenceNodeName, ObservableViewNoOverride windowView, Consumer<Node> nodeConsumer) {
        LOG.info("Restoring from: " + tabPreferenceNodeName);
        KometPreferences itemPreferences = windowPreferences.node(KOMET_NODES + tabPreferenceNodeName);
        itemPreferences.get(WindowComponent.WindowComponentKeys.FACTORY_CLASS).ifPresent(factoryClassName -> {
            try {
                Class<?> objectClass = Class.forName(factoryClassName);
                Class<? extends Annotation> annotationClass = Reconstructor.class;
                Object[] parameters = new Object[]{windowView, itemPreferences};
                WindowComponent windowComponent = (WindowComponent) Encodable.decode(objectClass, annotationClass, parameters);
                nodeConsumer.accept(windowComponent.getNode());

            } catch (Exception e) {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            }
        });
    }

    private static ImmutableList<DetachableTab> makeDefaultLeftTabs(ObservableViewNoOverride windowView) {

        GraphNavigatorNodeFactory navigatorNodeFactory = new GraphNavigatorNodeFactory();
        KometNode navigatorNode1 = navigatorNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab navigatorNode1Tab = new DetachableTab(navigatorNode1);


        PatternNavigatorFactory patternNavigatorNodeFactory = new PatternNavigatorFactory();

        KometNode patternNavigatorNode2 = patternNavigatorNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        DetachableTab patternNavigatorNode1Tab = new DetachableTab(patternNavigatorNode2);

        return Lists.immutable.of(navigatorNode1Tab, patternNavigatorNode1Tab);
    }

    private static ImmutableList<DetachableTab> makeDefaultCenterTabs(ObservableViewNoOverride windowView) {

        DetailsNodeFactory detailsNodeFactory = new DetailsNodeFactory();
        KometNode detailsNode1 = detailsNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.SUBSCRIBE.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        DetachableTab detailsNode1Tab = new DetachableTab(detailsNode1);
        // TODO: setting up tab graphic, title, and tooltip needs to be standardized by the factory...
        detailsNode1Tab.textProperty().bind(detailsNode1.getTitle());
        detailsNode1Tab.tooltipProperty().setValue(detailsNode1.makeToolTip());

        KometNode detailsNode2 = detailsNodeFactory.create(windowView,
                ActivityStreams.SEARCH, ActivityStreamOption.SUBSCRIBE.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab detailsNode2Tab = new DetachableTab(detailsNode2);

        KometNode detailsNode3 = detailsNodeFactory.create(windowView,
                ActivityStreams.UNLINKED, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab detailsNode3Tab = new DetachableTab(detailsNode3);

        ListNodeFactory listNodeFactory = new ListNodeFactory();
        KometNode listNode = listNodeFactory.create(windowView,
                ActivityStreams.LIST, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab listNodeNodeTab = new DetachableTab(listNode);

        TableNodeFactory tableNodeFactory = new TableNodeFactory();
        KometNode tableNode = tableNodeFactory.create(windowView,
                ActivityStreams.UNLINKED, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab tableNodeTab = new DetachableTab(tableNode);

        return Lists.immutable.of(detailsNode1Tab, detailsNode2Tab, detailsNode3Tab, listNodeNodeTab, tableNodeTab);
    }

    private static ImmutableList<DetachableTab> makeDefaultRightTabs(ObservableViewNoOverride windowView) {

        SearchNodeFactory searchNodeFactory = new SearchNodeFactory();
        KometNode searchNode = searchNodeFactory.create(windowView,
                ActivityStreams.SEARCH, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab newSearchTab = new DetachableTab(searchNode);

        ProgressNodeFactory progressNodeFactory = new ProgressNodeFactory();
        KometNode kometNode = progressNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab progressTab = new DetachableTab(kometNode);

        CompletionNodeFactory completionNodeFactory = new CompletionNodeFactory();
        KometNode completionNode = completionNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab completionTab = new DetachableTab(completionNode);

        return Lists.immutable.of(newSearchTab, progressTab, completionTab);
    }


    private static void generateWindowMenu(BorderPane kometRoot) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");


        MenuItem menuItemQuit = new MenuItem("Quit");
        KeyCombination quitKeyCombo = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        menuItemQuit.setOnAction(actionEvent -> Platform.exit());
        menuItemQuit.setAccelerator(quitKeyCombo);
        fileMenu.getItems().add(menuItemQuit);

        Menu editMenu = new Menu("Edit");

        Menu windowMenu = new Menu("Window");
        MenuItem minimizeWindow = new MenuItem("Minimize");
        KeyCombination minimizeKeyCombo = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);
        minimizeWindow.setOnAction(event -> {
            Stage obj = (Stage) kometRoot.getScene().getWindow();
            obj.setIconified(true);
        });
        minimizeWindow.setAccelerator(minimizeKeyCombo);
        windowMenu.getItems().add(minimizeWindow);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(editMenu);
        menuBar.getMenus().add(windowMenu);
        //hBox.getChildren().add(menuBar);
        Platform.runLater(() -> kometRoot.setTop(menuBar));
    }
}
