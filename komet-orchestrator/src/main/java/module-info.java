import dev.ikm.komet.orchestrator.KometOrchestrator;
import dev.ikm.orchestration.interfaces.StatusReportService;
import dev.ikm.tinkar.common.service.PluggableService;
import org.slf4j.spi.SLF4JServiceProvider;

module dev.ikm.komet.orchestrator {
    requires dev.ikm.komet.details;
    requires dev.ikm.komet.executor;
    requires dev.ikm.komet.list;
    requires dev.ikm.komet.preferences;
    requires dev.ikm.komet.progress;
    requires dev.ikm.komet.navigator;
    requires dev.ikm.komet.rules;

    requires dev.ikm.orchestration.interfaces;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.coordinate;
    requires dev.ikm.tinkar.plugin.service.boot;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires dev.ikm.komet.kview;
    requires dev.ikm.komet.search;

    uses PluggableService;
    uses SLF4JServiceProvider;

    provides StatusReportService with KometOrchestrator;

    opens dev.ikm.komet.orchestrator to javafx.fxml;
    exports dev.ikm.komet.orchestrator;
}