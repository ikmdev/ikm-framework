package dev.ikm.orchestration.provider.sync;

import dev.ikm.orchestration.interfaces.changeset.ChangeSetWriterService;
import dev.ikm.tinkar.common.service.TrackingCallable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The InitializeTask class represents a task that initializes a folder for synchronization, by
 * adding the necessary git configuration info. .
 * It extends the TrackingCallable class.
 */
class InitializeTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(InitializeTask.class);

    final Path changeSetFolder;

    /**
     * The InitializeTask class represents a task that initializes a folder for synchronization, by
     * adding the necessary git configuration info. It extends the TrackingCallable class.
     */
    public InitializeTask() {
        super(false, true);
        this.changeSetFolder = ChangeSetWriterService.changeSetFolder();

        updateTitle("Initializing " + changeSetFolder +
                " for synchronization");

        addToTotalWork(1);
    }

    /**
     * Computes the necessary git configuration for initializing a folder for synchronization.
     *
     * @return Always returns null.
     * @throws Exception If there is an error during computation.
     */
    @Override
    protected Void compute() throws Exception {
        try {

            InitCommand initCommand = Git.init();
            initCommand.setDirectory(changeSetFolder.toFile());
            initCommand.setInitialBranch("main");
            initCommand.call();

            Git git = Git.open(changeSetFolder.toFile());
            String configText = git.getRepository().getConfig().toText();
            // Workaround for: https://bugs.eclipse.org/bugs/show_bug.cgi?id=581483
            if (!configText.contains("x509")) {
                git.getRepository().getConfig().fromText(
                        """
[core]
	repositoryformatversion = 0
	filemode = true
	bare = false
	logallrefupdates = true
	ignorecase = true
	precomposeunicode = true
[submodule]
	active = .
[remote "git@github.com:kec/proto-zip.git"]
	url = https://kec@github.com/kec/proto-zip.git
	fetch = +refs/heads/*:refs/remotes/origin/*
	gtServiceAccountIdentifier = f1d16de25bf52ee2f3155f49b00a7906072cd8db40dcc853c60c4f9282320494
[gpg]
   format = x509
[commit]
    gpgsign = false
[branch "main"]
	remote = origin
	merge = refs/heads/main
                                  """
                );
                git.getRepository().getConfig().save();
            }
            completedUnitOfWork();
            return null;
        } catch (IllegalArgumentException | IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            return null;
        }
    }
}