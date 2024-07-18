/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.orchestration.provider.data;

import dev.ikm.orchestration.interfaces.Orchestrator;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import javafx.application.Platform;

import static dev.ikm.orchestration.interfaces.Lifecycle.RUNNING;

/**
 * The StartDataServiceTask class represents a task for starting a data service in an orchestrator.
 * It extends the TrackingCallable class and overrides the compute() method to perform the task.
 */
public class StartDataServiceTask extends TrackingCallable<Void> {
    final Orchestrator orchestrator;

    /**
     * The StartDataServiceTask class represents a task for starting a data service in an orchestrator.
     * It extends the TrackingCallable class and overrides the compute() method to perform the task.
     */
    public StartDataServiceTask(Orchestrator orchestrator) {
        super(false, true);
        this.orchestrator = orchestrator;
        updateTitle("Starting Data Service");
        updateMessage("Executing " + PrimitiveData.getController().controllerName());
        updateProgress(-1, -1);
    }

    /**
     * Computes the task for starting a data service in an orchestrator.
     * This method is called when the task is executed.
     *
     * @return null
     * @throws Exception if an error occurs during task execution
     */
    @Override
    protected Void compute() throws Exception {
        try {
            PrimitiveData.start();
            Platform.runLater(() -> this.orchestrator.lifecycleProperty().set(RUNNING));
            return null;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        } finally {
            updateTitle(PrimitiveData.getController().controllerName() + " completed");
            updateMessage("In " + durationString());
        }
    }
}
