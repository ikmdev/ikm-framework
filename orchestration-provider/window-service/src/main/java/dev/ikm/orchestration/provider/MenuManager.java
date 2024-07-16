package dev.ikm.orchestration.provider;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Manages the application window based on active stages.
 * Implements the ListChangeListener interface to listen for changes in the list of active stages.
 */
public class MenuManager implements ListChangeListener<Window> {
    private static final Logger LOG = LoggerFactory.getLogger(MenuManager.class);
    private static final MenuManager singleton = new MenuManager();
    static final ConcurrentHashMap<Stage, MenuBar> managedMenus = new ConcurrentHashMap<Stage, MenuBar>();
    static final CopyOnWriteArrayList<Stage> openStages = new CopyOnWriteArrayList<Stage>();

    private MenuManager() {
        Window.getWindows().addListener(this);
    }

    /**
     * Adds a stage and its associated menu bar to the MenuManager. This allows the MenuManager
     * to update the menus whenever a new stage is added.
     *
     * @param stage The Stage to be added.
     * @param menuBar The MenuBar associated with the Stage.
     */
    public static void addStage(Stage stage, MenuBar menuBar) {
        managedMenus.put(stage, menuBar);
        updateMenus();
    }

    /**
     * Method called when the observed list of windows has changed.
     * It handles various change scenarios, such as permutation, update, removal, and addition of windows.
     *
     * @param c the change object representing the change to the list of windows
     */
    @Override
    public void onChanged(Change<? extends Window> c) {
        while (c.next()) {
            if (c.wasPermutated()) {

                for (int oldIndex = c.getFrom(); oldIndex < c.getTo(); ++oldIndex) {
                    //permutate
                    int newIndex = c.getPermutation(oldIndex);
                    LOG.info("Permuted: " + oldIndex + " -> " + newIndex);
                }
            } else if (c.wasUpdated()) {
                //updated items
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    Window window = c.getList().get(i);
                    if (window instanceof Stage stage) {
                        LOG.info("Updated: " + stage.getTitle());
                    }
                }

            } else {
                for (Window removedWindow : c.getRemoved()) {
                    if (removedWindow instanceof Stage stage) {
                        LOG.info("Removed: {}", stage.getTitle());
                        managedMenus.remove(stage);
                        openStages.remove(stage);
                        updateMenus();
                    }
                }
                for (Window addedWindow : c.getAddedSubList()) {
                    if (addedWindow instanceof Stage stage) {
                        LOG.info("Added: {}", stage.getTitle());
                        getMenuBar(stage.getScene().getRoot()).ifPresentOrElse(
                                menuBar -> managedMenus.put(stage, menuBar),
                                () -> LOG.info("MenuBar not found. "));
                        openStages.add(stage);
                        updateMenus();
                    }
               }
            }
        }
    }

    /**
     * Updates the menus for all managed stages and menu bars.
     * If the "Window" menu does not exist in a menu bar, it will be added with the necessary menu items.
     * Otherwise, the menu items in the "Window" menu will be updated based on the current state of the application.
     */
    protected static void updateMenus() {
        managedMenus.forEach((stage, menuBar) -> {
            menuBar.getMenus().stream().filter(menu -> menu.getText().equals("Window")).findFirst().ifPresentOrElse(
                    menu -> addBringToFrontMenuItems(stage, menu),
                    () -> {
                        Platform.runLater(() -> {
                            Menu windowMenu = new Menu("Window");
                            menuBar.getMenus().add(windowMenu);
                            addBringToFrontMenuItems(stage, windowMenu);
                        });
                    });
        });
    }

    /**
     * Adds the "Bring to Front" menu items to the given window menu.
     *
     * @param windowStage The stage associated with the window menu.
     * @param windowMenu The menu to which the "Bring to Front" menu items will be added.
     */
    private static void addBringToFrontMenuItems(Stage windowStage, Menu windowMenu) {
        windowMenu.getItems().clear();
        windowMenu.getItems().clear();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        MenuItem newClassicKometWindow = new MenuItem("New Classic Komet Window");
        newClassicKometWindow.setOnAction(event -> Platform.runLater(new NewClassicKometWindowTask()));
        windowMenu.getItems().add(newClassicKometWindow);

        windowMenu.getItems().add(new SeparatorMenuItem());

        List<String> savedWindows = appPreferences.getList(WindowServiceKeys.SAVED_WINDOWS);
        savedWindows.stream().sorted(Collections.reverseOrder()).forEach(windowName -> {
            // Don't add those that are already open...
            if (openStages.stream().noneMatch(stage -> stage.getTitle().equals(windowName))) {
                Menu actOnWindow = new Menu(windowName);
                windowMenu.getItems().add(actOnWindow);

                MenuItem savedWindowMenuItem = new MenuItem("Restore");
                savedWindowMenuItem.setOnAction((ActionEvent event) ->
                        Platform.runLater(new RunThenUpdateWindowMenus(new RestoreClassicKometWindowTask(windowName))));
                actOnWindow.getItems().add(savedWindowMenuItem);

                MenuItem forgetWindowMenuItem = new MenuItem("Forget");
                forgetWindowMenuItem.setOnAction((ActionEvent event) ->
                        Platform.runLater(new RunThenUpdateWindowMenus(new ForgetWindowTask(windowName))));
                actOnWindow.getItems().add(forgetWindowMenuItem);
            }
        });
        windowMenu.getItems().add(new SeparatorMenuItem());
        MenuItem nextWindow = new MenuItem("Cycle through windows");
        KeyCombination nextWindowKeyCombo = new KeyCodeCombination(KeyCode.BACK_QUOTE, KeyCombination.META_DOWN);
        nextWindow.setAccelerator(nextWindowKeyCombo);
        nextWindow.setOnAction(event -> {
            List<Stage> sortedStages = managedMenus.keySet().stream()
                    .sorted((s1, s2) -> s1.getTitle().compareTo(s2.getTitle()))
                    .collect(Collectors.toList());
            int currentIndex = sortedStages.indexOf(windowStage);
            int nextIndex = (currentIndex + 1) % sortedStages.size();
            Stage nextStage = sortedStages.get(nextIndex);
            nextStage.toFront();
        });
        windowMenu.getItems().add(nextWindow);
        MenuItem bringAllToFront = new MenuItem("Bring all to front");
        bringAllToFront.setOnAction((ActionEvent event) -> {
            managedMenus.keySet().forEach(Stage::toFront);
        });
        windowMenu.getItems().add(bringAllToFront);
        windowMenu.getItems().add(new SeparatorMenuItem());
        managedMenus.entrySet().stream().sorted((o1, o2) -> o2.getKey().getTitle().compareTo(o1.getKey().getTitle()))
                        .forEach(stageMenuBarEntry -> {
                            MenuItem toFrontMenuItem = new MenuItem(stageMenuBarEntry.getKey().getTitle());
                            toFrontMenuItem.setOnAction((ActionEvent event) -> {
                                stageMenuBarEntry.getKey().toFront();
                            });
                            windowMenu.getItems().add(toFrontMenuItem);
                        });
    }

    /**
     * Retrieves the MenuBar component associated with the given Node.
     *
     * @param node The Node to search for a MenuBar.
     * @return An Optional object containing the MenuBar, if found, or empty if not.
     */
    private Optional<MenuBar> getMenuBar(Node node) {
        switch (node) {
            case MenuBar menuBar: return Optional.of(menuBar);
            case Parent parent:
                for (Node child: parent.getChildrenUnmodifiable()) {
                    Optional<MenuBar> menuBarOptional = getMenuBar(child);
                    if (menuBarOptional.isPresent()) {
                        return menuBarOptional;
                    }
                }
                break;
            default:
                // nothing to do...
        };
        return Optional.empty();
    }

    /**
     * This class is a private static class that extends the {@link Task} class. It is used to run a task
     * and then update the menus of all managed stages and menu bars.
     */
    private static class RunThenUpdateWindowMenus extends Task<Void> {
        Task taskToRun;

        /**
         * A wrapper task that will run a task and then update the menus
         * of all managed stages and menu bars.
         *
         * @param taskToRun The Task to be run.
         */
        public RunThenUpdateWindowMenus(Task taskToRun) {
            this.taskToRun = taskToRun;
        }

        /**
         * Executes the provided task and updates the menus for all managed stages and menu bars.
         *
         * @return Always returns null.
         * @throws Exception if an error occurs during task execution.
         */
        @Override
        protected Void call() throws Exception {
            taskToRun.run();
            Platform.runLater(() -> {
                updateMenus();
            });
            return null;
        }
    }
}

