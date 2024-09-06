package dev.ikm.orchestration.provider.changeset.writer;

import dev.ikm.orchestration.interfaces.changeset.ChangeSetWriterService;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.entity.EntityService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO give the change set writer its own thread.

/**
 * A class that manages the writing of change sets.
 * Implements the Subscriber<Integer> and ChangeSetWriterService interfaces.
 */
public class ChangeSetWriterManager implements Subscriber<Integer>, ChangeSetWriterService {
    public static final DateTimeFormatter SHORT_MIN_FOR_FILE_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH꞉mm");
    final EntityService entityService;
    Path changeSetFolder;
    AtomicBoolean enabled = new AtomicBoolean(false);
    ChangeSetWriter changeSetWriter;

    /**
     * The ChangeSetWriterManager class manages the ChangeSetWriter and its configuration.
     */
    public ChangeSetWriterManager() {
        this.entityService = PluggableService.first(EntityService.class);
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        optionalDataStoreRoot.ifPresentOrElse(dataStoreRoot -> {
            ChangeSetWriterManager.this.changeSetFolder = Paths.get(dataStoreRoot.getAbsolutePath(), "changesets");
            try {
                Files.createDirectories(changeSetFolder);
                this.changeSetWriter = new ChangeSetWriter(entityService, getChangeSetFile());
                this.enabled.set(true);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create write folder", e);
            }
        }, () -> {
            throw new IllegalStateException("No ServiceKeys.DATA_STORE_ROOT provided. ");
        });

        this.entityService.addSubscriberWithWeakReference(this);

        //TODO develop way to register shutdown process with the datastore... Then remove the shutdown hook...
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (changeSetWriter != null) {
                try {
                    changeSetWriter.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        //TODO create a rollover strategy when a change set exceeds a size.
    }


    /**
     * Returns the file for the change set.
     *
     * @return the file for the change set
     */
    private final File getChangeSetFile() {
        return changeSetFolder.resolve(LocalDateTime.now().format(SHORT_MIN_FOR_FILE_FORMATTER) + "~" + UUID.randomUUID() + ".proto.zip").toFile();
    }

    /**
     * Processes the next native identifier received by the subscriber,
     * by writing a change set corresponding to the identified component to disk.
     *
     * @param nid the integer value to process
     */
    @Override
    public void onNext(Integer nid) {
        if (enabled.get()) {
            this.changeSetWriter.onNext(nid);
        }
    }

    /**
     * Pauses the ChangeSetWriter by disabling further writes and closing the writer.
     * Throws an IOException if an error occurs during the close operation.
     * The underlying file writer(s) are closed and further writes to disk are blocked until resume is called.
     * Ensure that if pause() is called, resume is called from the same thread.
     *
     * @throws IOException if an error occurs during the pause operation
     */
    @Override
    public void pause() throws IOException {
        enabled.set(false);
        // Close the writer.
        this.changeSetWriter.close();
    }

    /**
     * Resumes the ChangeSetWriter by enabling further writes and opening a new writer.
     *
     * @throws IOException if an error occurs while opening the writer
     */
    @Override
    public void resume() throws IOException {
        enabled.set(true);
        // open a new writer
        this.changeSetWriter = new ChangeSetWriter(entityService, getChangeSetFile());
    }

    /**
     * Returns the path to the folder that contains the change sets.
     *
     * @return the path to the change set folder
     */
    @Override
    public Path getChangeSetFolder() {
        return this.changeSetFolder;
    }

    /**
     * Returns the current write status of the ChangeSetWriter.
     *
     * @return the current write status, {@code true} if enabled or {@code false} if disabled
     */
    @Override
    public boolean getWriteStatus() {
        return enabled.get();
    }

    /**
     * Writes a change set corresponding to the identified component to disk.
     *
     * @param nid the integer value to process
     */
    @Override
    public void write(int nid) {
        this.changeSetWriter.onNext(nid);
    }
}
