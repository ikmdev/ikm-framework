package dev.ikm.orchestration.interfaces;

/**
 * The Lifecycle enum represents the different stages of the lifecycle of an application.
 * It is used to track the state of the application and control its execution flow.
 */
public enum Lifecycle {
    EXECUTOR_START,
    STARTING,
    SELECT_DATA_SOURCE,
    SELECTED_DATA_SOURCE,
    LOADING_DATA_SOURCE,
    RUNNING,
    SHUTDOWN;
}
