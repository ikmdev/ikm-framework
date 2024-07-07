import dev.ikm.orchestration.interfaces.SelectDataService;
import dev.ikm.orchestration.provider.data.select.SelectDataServiceProvider;

/**
 * The module declaration for the select data source provider module.
 * This module provides the functionality to select a data service for an application run.
 *
 * This module requires the following modules:
 * - dev.ikm.orchestration.interfaces
 * - dev.ikm.komet.framework
 * - dev.ikm.komet.preferences
 * - dev.ikm.komet.progress
 *
 * This module opens the package "dev.ikm.orchestration.provider.data.select" to JavaFX,
 * which allows it to be accessed by JavaFX controllers.
 *
 * This module also provides the implementation class "SelectDataServiceProvider" for the interface "SelectDataService".
 */
module dev.ikm.orchestration.provider.datasource.select {
    requires dev.ikm.orchestration.interfaces;
    requires dev.ikm.komet.framework;
    requires dev.ikm.komet.preferences;
    requires dev.ikm.komet.progress;

    opens dev.ikm.orchestration.provider.data.select to javafx.fxml, javafx.graphics;

    provides SelectDataService with SelectDataServiceProvider;
}