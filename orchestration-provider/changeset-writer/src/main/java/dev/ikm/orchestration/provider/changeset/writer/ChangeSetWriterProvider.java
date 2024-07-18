package dev.ikm.orchestration.provider.changeset.writer;

import dev.ikm.orchestration.interfaces.changeset.ChangeSetWriterService;

/**
 * A provider class for the ChangeSetWriterService.
 */
public class ChangeSetWriterProvider {
    static final ChangeSetWriterService changeSetWriterService = new ChangeSetWriterManager();

    /**
     * Provides an instance of the ChangeSetWriterService.
     *
     * @return an instance of the ChangeSetWriterService
     */
    public static ChangeSetWriterService provider() {
        return changeSetWriterService;
    }
}