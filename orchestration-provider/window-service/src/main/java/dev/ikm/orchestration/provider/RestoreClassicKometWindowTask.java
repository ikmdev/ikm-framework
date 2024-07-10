package dev.ikm.orchestration.provider;

import javafx.concurrent.Task;

public class RestoreClassicKometWindowTask extends Task<Void> {
    final String windowName;

    public RestoreClassicKometWindowTask(String windowName) {
        this.windowName = windowName;
    }

    @Override
    protected Void call() throws Exception {
        return null;
    }
}
