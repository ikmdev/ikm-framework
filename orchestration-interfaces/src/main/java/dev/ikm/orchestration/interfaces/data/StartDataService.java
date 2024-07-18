package dev.ikm.orchestration.interfaces.data;

import dev.ikm.orchestration.interfaces.Orchestrator;

import java.util.concurrent.Future;

/**
 * The StartDataService interface represents a service that provides the functionality to start a data service task
 * in an orchestrator.
 */
public interface StartDataService {
    /**
     * Starts a data service task in an orchestrator.
     *
     * @param orchestrator the orchestrator where the data service task will be started
     * @return a Future object representing the completion of the task
     */
    Future<Void> startDataServiceTask(Orchestrator orchestrator);
}
