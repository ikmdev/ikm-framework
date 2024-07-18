package dev.ikm.orchestration.interfaces.changeset;


import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * {@link ChangeSetWriterService}
 *
 * Any @Service annotated class which implements this interface will get the notifications below, when
 * index events happen.
 *
 */
public interface ChangeSetWriterService {

    /**
     * flush any unwritten data, close the underlying file writer(s), and block further writes to disk until
     * resume is called. This feature is useful when you want to ensure the file on disk doesn't change while another thread picks
     * up the file and pushes it to git, for example.
     *
     * Ensure that if pause() is called, that resume is called from the same thread.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void pause()
            throws IOException;

    /**
     * open the file writer (closed by a {@link #pause()}) and unblock any blocked write calls.
     * Ensure that if pause() is called, that resume is called from the same thread.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void resume()
            throws IOException;

    //~--- get methods ---------------------------------------------------------

    /**
     * Return the path to the folder that contains the changesets.
     *
     * @return the write folder
     */
    Path getChangeSetFolder();

    /**
     * Determine if the writer in the service is disabled or enabled for writing.
     *
     * @return {@code true} if enabled or {@code false} if disabled.
     */
    boolean getWriteStatus();

    /**
     * Write the entity with the provided nid to the change set.
     * @param nid
     */
    void write(int nid);

    /**
     * Creates the folder for storing change sets. If the data store root is available,
     * it creates the change set folder by appending "changesets" to the data store root path.
     * Otherwise, it throws an IllegalStateException.
     *
     * @return the path to the change set folder
     * @throws RuntimeException if an IO exception occurs while creating the directories
     * @throws IllegalStateException if the data store root is not provided
     */
    static Path changeSetFolder() {
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (optionalDataStoreRoot.isPresent()) {
            Path changeSetFolder = Paths.get(optionalDataStoreRoot.get().getAbsolutePath(), "changesets");
            try {
                Files.createDirectories(changeSetFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return changeSetFolder;
        }
        throw new IllegalStateException("No ServiceKeys.DATA_STORE_ROOT provided. ");
    }
}


