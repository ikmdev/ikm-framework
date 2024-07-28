package dev.ikm.komet.orchestrator;

import dev.ikm.orchestration.interfaces.OrchestrationService;

public class OrchestrationProvider {
    public static OrchestrationService provider() {
        return KometOrchestrator.kometOrchestrator;
    }
}
