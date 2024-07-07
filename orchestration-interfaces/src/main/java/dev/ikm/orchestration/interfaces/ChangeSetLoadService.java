package dev.ikm.orchestration.interfaces;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * The ChangeSetLoadService interface provides methods for triggering a re-read of change set files
 * and retrieving information about the processed change sets.
 */
public interface ChangeSetLoadService {
    /**
     * Call to trigger a re-read of changeset files - which may be necessary after a remote sync, for example.
     * returns the number of files loaded
     *
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     */
    int readChangesetFiles()
            throws IOException;

    /**
     *
     * @return map of the filename of a change set, and the size of the change set
     * when last processed.
     */
    ConcurrentMap<String, Integer> getProcessedChangesets();
}