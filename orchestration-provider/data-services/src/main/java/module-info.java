import dev.ikm.orchestration.interfaces.data.SelectDataService;
import dev.ikm.orchestration.interfaces.data.StartDataService;
import dev.ikm.orchestration.provider.data.SelectDataServiceProvider;
import dev.ikm.orchestration.provider.data.StartDataServiceProvider;

/**
 *
 */
module dev.ikm.orchestration.provider.datasource.select {
    requires dev.ikm.komet.framework;
    requires dev.ikm.komet.preferences;
    requires dev.ikm.komet.progress;
    requires dev.ikm.orchestration.interfaces;
    requires dev.ikm.tinkar.common;
    requires javafx.graphics;

    opens dev.ikm.orchestration.provider.data to javafx.fxml, javafx.graphics;

    provides StartDataService with StartDataServiceProvider;
    provides SelectDataService with SelectDataServiceProvider;
}