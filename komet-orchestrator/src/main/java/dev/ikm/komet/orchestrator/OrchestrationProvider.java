package dev.ikm.komet.orchestrator;

import dev.ikm.orchestration.interfaces.OrchestrationService;

/**
 * The OrchestrationProvider class is responsible for providing an instance of OrchestrationService.
 * It provides a static method that returns the instance of the OrchestrationService implementation.
 */
public class OrchestrationProvider {
    public static OrchestrationService provider() {
        return KometOrchestrator.kometOrchestrator;
    }
}
