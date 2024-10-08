package dev.ikm.orchestration.provider.data;

import dev.ikm.komet.framework.ScreenInfo;
import dev.ikm.orchestration.interfaces.OrchestrationService;
import dev.ikm.orchestration.interfaces.data.SelectDataService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.util.concurrent.Future;

import static dev.ikm.orchestration.interfaces.CssService.CSS_LOCATION;
import static dev.ikm.orchestration.interfaces.Lifecycle.SHUTDOWN;

/**
 * The {@code SelectDataServiceProvider} class is an implementation of the {@link SelectDataService} interface.
 * It provides the functionality to perform the select data task asynchronously.
 */
public class SelectDataServiceProvider implements SelectDataService {

    /**
     * Executes the {@code SelectDataServiceProviderTask} asynchronously to perform the select data task.
     *
     * @param orchestrator the orchestrationService object representing the application
     * @return a {@code Future} object that will be completed with a {@code null} value when the task is done
     */
    @Override
    public Future<Void> selectDataServiceTask(OrchestrationService orchestrator) {
        SelectDataServiceProviderTask theTask = new SelectDataServiceProviderTask(orchestrator);
        Platform.runLater(theTask);
        return theTask;
    }

    /**
     * The SelectDataServiceProviderTask class represents a task that is responsible for selecting a data service provider.
     * This task is executed asynchronously and updates the user interface accordingly.
     */
    private class SelectDataServiceProviderTask extends Task<Void> {
        private final OrchestrationService orchestrationService;
        public SelectDataServiceProviderTask(OrchestrationService orchestrationService) {
            this.orchestrationService = orchestrationService;
        }

        /**
         * Executes the task responsible for selecting a data source provider. This task is executed asynchronously
         * and updates the user interface accordingly.
         *
         * @return Always returns null.
         * @throws Exception if an error occurs during the execution of the task.
         */
        @Override
        protected Void call() throws Exception {
            SelectDataSourceController selectDataSourceController = new SelectDataSourceController(orchestrationService);
            FXMLLoader sourceLoader = new FXMLLoader();
            sourceLoader.setController(selectDataSourceController);
            sourceLoader.setLocation(SelectDataServiceProvider.class.getResource("SelectDataSource.fxml"));
            BorderPane sourceRoot = sourceLoader.load();
            Scene sourceScene = new Scene(sourceRoot, 600, 400);

            Module graphicsModule = ModuleLayer.boot()
                    .findModule("dev.ikm.komet.framework")
                    // Optional<Module> at this point
                    .orElseThrow();

            sourceScene.getStylesheets()
                    .add(graphicsModule.getClassLoader().getResource(CSS_LOCATION).toString());
            orchestrationService.primaryStage().setScene(sourceScene);
            orchestrationService.primaryStage().setTitle("KOMET Startup");

            orchestrationService.primaryStage().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ScreenInfo.mouseIsPressed(true);
                ScreenInfo.mouseWasDragged(false);
            });
            orchestrationService.primaryStage().addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                ScreenInfo.mouseIsPressed(false);
                ScreenInfo.mouseIsDragging(false);
            });
            orchestrationService.primaryStage().addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
                ScreenInfo.mouseIsDragging(true);
                ScreenInfo.mouseWasDragged(true);
            });

            // Ensure app is shutdown gracefully. Once state changes it calls appStateChangeListener.
            orchestrationService.primaryStage().setOnCloseRequest(windowEvent -> {
                orchestrationService.lifecycleProperty().set(SHUTDOWN);
            });
            orchestrationService.primaryStage().show();
            return null;
        }
    }

}
