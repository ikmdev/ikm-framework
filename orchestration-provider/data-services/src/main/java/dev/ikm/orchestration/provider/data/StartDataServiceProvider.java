package dev.ikm.orchestration.provider.data;

import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.orchestration.interfaces.Orchestrator;
import dev.ikm.orchestration.interfaces.data.StartDataService;
import dev.ikm.tinkar.common.service.TinkExecutor;

import java.util.concurrent.Future;

/**
 * The StartDataServiceProvider class implements the StartDataService interface and provides the functionality to start a data service task in an orchestrator.
 */
public class StartDataServiceProvider implements StartDataService {

    /**
     * Starts a data service task in an orchestrator.
     *
     * @param orchestrator the orchestrator where the data service task will be started
     * @return a Future object representing the completion of the task
     */
    @Override
    public Future<Void> startDataServiceTask(Orchestrator orchestrator) {
        TaskWrapper<Void> startDataServiceTask = TaskWrapper.make(new StartDataServiceTask(orchestrator));
        TinkExecutor.threadPool().execute(startDataServiceTask);
        return startDataServiceTask;
    }
}
