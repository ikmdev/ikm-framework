package dev.ikm.orchestration.provider.window.menu;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.orchestration.interfaces.journal.NewJournalService;
import javafx.application.Platform;

import java.util.List;

public class NewJournalProvider implements NewJournalService {

    @Override
    public void make(PrefX prefX, List<JournalController> journalControllersList) {
        NewJournalViewTask newJournalViewTask = new NewJournalViewTask(prefX, journalControllersList);
        if (Platform.isFxApplicationThread()) {
            try {
                newJournalViewTask.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Platform.runLater(newJournalViewTask);
        }
    }
}
