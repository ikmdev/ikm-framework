package dev.ikm.orchestration.interfaces.data;

import dev.ikm.orchestration.interfaces.Orchestrator;

import java.util.concurrent.Future;

/**
 * The {@code SelectDataService} interface represents a service for selecting the data service for an application
 * run.
 *
 * It provides the {@link #selectDataServiceTask(Orchestrator)} method, which takes an {@link Orchestrator} object
 * and performs the select data task asynchronously.
 *
 * @since 1.0
 */
public interface SelectDataService {
    /**
     * Performs the task of selecting the data service for an application run asynchronously.
     *
     * @param orchestrator the orchestrator object representing the application
     * @return a CompletableFuture that will be completed with null value when the task is done
     */
    Future<Void> selectDataServiceTask(Orchestrator orchestrator);
}
