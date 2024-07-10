package dev.ikm.orchestration.provider;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.orchestration.interfaces.WindowMenuProvider;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Window;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WindowServiceStaticMenuProvider implements WindowMenuProvider {

    private static final ConcurrentHashMap<Window, Menu> windowMenuMap = new ConcurrentHashMap<>();

    @Override
    public Menu getWindowMenu(Window window) {
        return windowMenuMap.computeIfAbsent(window, w -> getWindowMenu());
    }

    private static Menu getWindowMenu() {
        Menu windowMenu = new Menu("Window");
        return setWindowMenuItems(windowMenu);
    }

    private static Menu setWindowMenuItems(Menu windowMenu) {
        windowMenu.getItems().clear();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        MenuItem newClassicKometWindow = new MenuItem("New Classic Komet Window");
        newClassicKometWindow.setOnAction(event -> Platform.runLater(new NewClassicKometWindowTask()));
        windowMenu.getItems().add(newClassicKometWindow);

        MenuItem separator = new SeparatorMenuItem();
        windowMenu.getItems().add(separator);

        List<String> savedWindows = appPreferences.getList(WindowServiceKeys.SAVED_WINDOWS);
        savedWindows.forEach(windowName -> {
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
        });
        return windowMenu;
    }

    private static class RunThenUpdateWindowMenus extends Task<Void> {
        Task taskToRun;

        public RunThenUpdateWindowMenus(Task taskToRun) {
            this.taskToRun = taskToRun;
        }

        @Override
        protected Void call() throws Exception {
            taskToRun.run();
            Platform.runLater(() -> {
                windowMenuMap.forEach((window, menu) -> setWindowMenuItems(menu));
            });
            return null;
        }
    }
}
