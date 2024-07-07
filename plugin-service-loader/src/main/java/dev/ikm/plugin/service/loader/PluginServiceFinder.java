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
package dev.ikm.plugin.service.loader;

import dev.ikm.plugin.layer.PluggableServiceManager;
import dev.ikm.plugin.layer.PluginLifecycleListener;
import dev.ikm.tinkar.common.service.PluginServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * The PluginServiceFinder class is responsible for loading and managing
 * pluggable services using the Java module system.
 *
 * It implements the PluginServiceLoader and PluginLifecycleListener interfaces
 * to provide the necessary functionality.
 *
 */
public class PluginServiceFinder implements PluginServiceLoader, PluginLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(PluginServiceFinder.class);

    @Override
    public <S> ServiceLoader<S> loader(Class<S> service) {
        return ServiceLoader.load(PluginServiceFinder.class.getModule().getLayer(), service);
    }

    /**
     * Ensures that the specified service is registered in the Java module system.
     * <p>
     * This method checks if the current class's module can use the given service.
     * If not, it adds the service to the uses clause of the module.
     *
     * @param service the service class to be checked
     * @return true if the service was added to the uses clause of the module
     *         (meaning it was not already included for this module),
     *         false otherwise
     */
    @Override
    public boolean ensureUses(Class<?> service) {
        if (!this.getClass().getModule().canUse(service)) {
            this.getClass().getModule().addUses(service);
            return true;
        }
        return false;
    }

    @Override
    public void pluginLayerAdded(String pluginLayerName, ModuleLayer pluginLayer) {
        PluggableServiceManager.setServiceProvider(this);
        LOG.info("added plugin layer: " + pluginLayerName + ": " + pluginLayer);
    }

    @Override
    public void pluginLayerBeingRemoved(String pluginLayerName, ModuleLayer pluginLayer) {
        LOG.info("removing plugin layer: " + pluginLayerName + ": " + pluginLayer);
    }
}
