package dev.ikm.orchestration.provider.sync;

import dev.ikm.orchestration.interfaces.changeset.ChangeSetWriterService;
import dev.ikm.orchestration.provider.sync.credential.PluginCredentialProvider;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.TrackingCallable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

class PullTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(PullTask.class);

    final Path changeSetFolder = ChangeSetWriterService.changeSetFolder();

    public PullTask() {
        updateTitle("Pulling files from server");
        addToTotalWork(3);
    }

    @Override
    protected Void compute() throws Exception {
        try {
            Git git = Git.open(changeSetFolder.toFile());

            PullCommand pullCommand = git.pull();
            pullCommand.setProgressMonitor(new JGitProgressMonitor());
            pullCommand.setRemoteBranchName("main");
            pullCommand.setCredentialsProvider(
                    new PluginCredentialProvider());
            PullResult result = pullCommand.call();

            return null;
        } catch (IllegalArgumentException | IOException ex) {
            AlertStreams.dispatchToRoot(ex);
        }
        return null;
    }

}