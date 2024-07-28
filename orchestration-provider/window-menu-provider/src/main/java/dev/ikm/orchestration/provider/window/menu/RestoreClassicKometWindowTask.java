package dev.ikm.orchestration.provider.window.menu;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.concurrent.Task;
import javafx.stage.Stage;

/**
 * The RestoreClassicKometWindowTask class is a task that restores a classic Komet window based on its window name.
 * This task is executed in a background thread.
 */
public class RestoreClassicKometWindowTask extends Task<Void> {
    final String windowName;

    /**
     * Constructs a new RestoreClassicKometWindowTask object with the specified window name.
     *
     * @param windowName The name of the window to be restored.
     */
    public RestoreClassicKometWindowTask(String windowName) {
        this.windowName = windowName;
    }

    /**
     * Restores a classic Komet window based on its window name.
     *
     * @return Always returns null.
     * @throws Exception If an exception occurs during the execution of the method.
     */
    @Override
    protected Void call() throws Exception {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(windowName);

        Stage stage = new Stage();
        stage.setTitle(windowName);

        NewClassicKometWindowTask.loadFromPreferences(stage, windowPreferences);
        WindowMenuManager.updateMenus();
        return null;
    }
}
