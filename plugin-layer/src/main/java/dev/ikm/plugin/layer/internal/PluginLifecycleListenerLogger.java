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
package dev.ikm.plugin.layer.internal;

import dev.ikm.plugin.layer.PluginLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link PluginLifecycleListener} interface and logs events related to plugin layer addition and removal.
 */
public class PluginLifecycleListenerLogger implements PluginLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(PluginLifecycleListenerLogger.class);

    /**
     * This method is called when a plugin layer is added.
     *
     * @param pluginLayerName the name of the added plugin layer
     * @param pluginLayer     the added plugin layer
     */
    @Override
    public void pluginLayerAdded(String pluginLayerName, ModuleLayer pluginLayer) {
        LOG.info("Plugin layer added: " + pluginLayer);
    }

    @Override
    public void pluginLayerBeingRemoved(String pluginLayerName, ModuleLayer pluginLayer) {
        // unregister plugins of _this_ layer
        LOG.info("Plugin layer removed: " + pluginLayer);
    }
}
