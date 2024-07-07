import dev.ikm.orchestration.interfaces.StartDataService;
import dev.ikm.orchestration.provider.data.start.StartDataServiceProvider;

open module dev.ikm.orchestration.provider.data.start {
    requires dev.ikm.orchestration.interfaces;
    requires javafx.graphics;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.komet.framework;
    provides StartDataService with StartDataServiceProvider;
}