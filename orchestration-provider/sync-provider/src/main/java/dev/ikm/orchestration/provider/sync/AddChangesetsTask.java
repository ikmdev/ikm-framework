package dev.ikm.orchestration.provider.sync;

import dev.ikm.orchestration.interfaces.changeset.ChangeSetWriterService;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.TrackingCallable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * {@link AddChangesetsTask} is a class that adds changesets for synchronization.
 * It extends the {@link TrackingCallable} class.
 */
public class AddChangesetsTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(AddChangesetsTask.class);

    final Path changeSetFolder = ChangeSetWriterService.changeSetFolder();
    /**
     * The AddChangesetsTask class represents a task that adds changesets for synchronization.
     * It extends the TrackingCallable class.
     */
    public AddChangesetsTask() {
        super(false, true);

        updateTitle("Adding Changesets for synchronization");

        addToTotalWork(3);
    }

    /**
     * Computes and performs the task of adding changesets for synchronization.
     * Extends the TrackingCallable class. This task pauses the ChangeSetWriterService
     * which ensures that only completed zip files will be synchronized.
     *
     */
    @Override
    protected Void compute() {
        this.updateMessage("Pausing change set writer service");
        ChangeSetWriterService changeSetWriterService = PluggableService.first(ChangeSetWriterService.class);
        silentPauseChangesetWriter(changeSetWriterService);

        try {
            Git git = Git.open(changeSetFolder.toFile());

            ImmutableList<String> filesToAdd = filesToAdd(changeSetFolder, ".proto.zip");
            AddCommand addCommand = git.add();
            addCommand.setUpdate(false);
            filesToAdd.forEach(s -> {
                addCommand.addFilepattern(s);
                this.updateMessage("Adding changeset " + s);
            });

            DirCache dirCache = addCommand.call();
            CommitCommand commitCommand = git.commit();
            commitCommand.setMessage("Manual changeset add");
            commitCommand.setAll(true);
            RevCommit revCommit = commitCommand.call();
            LOG.info(String.format("Successfully Committed %i files: %s", filesToAdd.size(), filesToAdd));
        } catch (IOException | GitAPIException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            AlertStreams.dispatchToRoot(ex);
        } finally {
            silentResumeChangesetWriter(changeSetWriterService);
            this.updateMessage("Resumed change set writer service");
        }
        return null;
    }

    // TODO: Implement better handling of IOException caused by closing changeset writer when it is not running
    private void silentPauseChangesetWriter(ChangeSetWriterService changeSetWriterService) {
        try {
            changeSetWriterService.pause();
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    // TODO: Implement better handling of IOException caused by resuming changeset writer when it is already running
    private void silentResumeChangesetWriter(ChangeSetWriterService changeSetWriterService) {
        try {
            changeSetWriterService.resume();
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Computes the list of files to add for synchronization. Searches the provided directory and all subdirectories.
     *
     * @param directory The directory to search for files.
     * @param pattern   The file pattern to use for searching.
     * @return An immutable list of file paths to add for synchronization.
     */
    ImmutableList<String> filesToAdd(Path directory, String pattern){
        return filesToAdd(directory, pattern, Lists.mutable.empty()).toImmutable();
    }
    /**
     * Computes the list of files to add for synchronization using a recursive call to ensure
     * all subdirectories are processed. .
     *
     * @param directory The directory to search for files.
     * @param pattern   The file pattern to use for searching.
     * @param filesToAdd The list of files to add for synchronization.
     * @return An mutable list of file paths to add for synchronization.
     */
    MutableList<String> filesToAdd(Path directory, String pattern, MutableList<String> filesToAdd){
        File[] matchingFiles = directory.toFile().listFiles((dir, name) -> name.endsWith(pattern));

        Arrays.stream(matchingFiles).forEach(f -> filesToAdd.add(
                changeSetFolder.relativize(f.toPath()).toString()));

        List<File> directories = Arrays.stream(directory.toFile().listFiles()).filter(file -> file.isDirectory() &! file.isHidden()).toList();
        directories.forEach(dir -> filesToAdd(dir.toPath(), pattern, filesToAdd));
        return filesToAdd;
    }
}