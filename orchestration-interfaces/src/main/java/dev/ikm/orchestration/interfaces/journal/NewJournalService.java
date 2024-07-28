package dev.ikm.orchestration.interfaces.journal;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;

import java.util.List;

public interface NewJournalService {
    void make(PrefX prefX, List<JournalController> journalControllersList);
}
