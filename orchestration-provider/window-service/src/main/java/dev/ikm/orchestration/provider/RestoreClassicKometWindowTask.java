package dev.ikm.orchestration.provider;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import static dev.ikm.orchestration.provider.WindowServiceStaticMenuProvider.removeWindow;

public class RestoreClassicKometWindowTask extends Task<Void> {
    final String windowName;

    public RestoreClassicKometWindowTask(String windowName) {
        this.windowName = windowName;
    }

    @Override
    protected Void call() throws Exception {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(windowName);

        Stage stage = new Stage();
        stage.setTitle(windowName);

        NewClassicKometWindowTask.loadFromPreferences(stage, windowPreferences);
        removeWindow(windowName);
        return null;
    }
}
