package dev.ikm.orchestration.interfaces.journal;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;

import java.util.List;

/**
 * The NewJournalService interface provides a way to create a new journal with the given parameters.
 * Implementations of this interface will customize the behavior of the journal creation process.
 *
 */
public interface NewJournalService {
    /**
     * Makes a new journal with the given PrefX and list of JournalControllers.
     *
     * @param prefX                the PrefX instance to use for creating the journal
     * @param journalControllersList the list of JournalControllers to add the newly created journal to
     */
    void make(PrefX prefX, List<JournalController> journalControllersList);
}
