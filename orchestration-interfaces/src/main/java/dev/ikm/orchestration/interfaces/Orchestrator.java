package dev.ikm.orchestration.interfaces;

import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

/**
 * The Orchestrator interface represents an orchestrator that manages the lifecycle and execution flow
 * of an application.
 */
public interface Orchestrator {
    /**
     * Returns the primary stage of the application.
     *
     * @return the primary stage
     */
    Stage primaryStage();
    /**
     * Retrieves the lifecycle property of the Orchestrator.
     *
     * @return the lifecycle property of the Orchestrator
     */
    SimpleObjectProperty<Lifecycle> lifecycleProperty();
}
