package dev.ikm.orchestration.provider.window.menu;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.orchestration.interfaces.journal.NewJournalService;
import javafx.application.Platform;

import java.util.List;

/**
 * The NewJournalProvider class is an implementation of the NewJournalService interface.
 * It provides a way to create a new journal with the given parameters.
 */
public class NewJournalProvider implements NewJournalService {

    /**
     * Creates a new journal with the given parameters.
     *
     * @param prefX                the PrefX object
     * @param journalControllersList the list of JournalController objects
     */
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
