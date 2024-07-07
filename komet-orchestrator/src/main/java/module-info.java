import dev.ikm.komet.orchestrator.KometOrchestrator;
import dev.ikm.orchestration.interfaces.StatusReportService;
import dev.ikm.tinkar.common.service.PluggableServiceLoader;
import org.slf4j.spi.SLF4JServiceProvider;

module dev.ikm.komet.orchestrator {
    requires dev.ikm.komet.executor;
    requires dev.ikm.komet.preferences;
    requires dev.ikm.komet.progress;
    requires dev.ikm.orchestration.interfaces;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.coordinate;
    requires dev.ikm.tinkar.plugin.service.boot;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;

    uses PluggableServiceLoader;
    uses SLF4JServiceProvider;

    provides StatusReportService with KometOrchestrator;

    opens dev.ikm.komet.orchestrator to javafx.fxml;
    exports dev.ikm.komet.orchestrator;
}