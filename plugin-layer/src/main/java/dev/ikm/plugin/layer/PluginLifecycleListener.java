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
package dev.ikm.plugin.layer;

/**
 * Implementations get notified about the addition or removal of plug-in layers.
 * Retrieved via the service loader API.
 */
public interface PluginLifecycleListener {

    /**
     * Notifies registered PluginLifecycleListeners about the addition of a plugin layer.
     *
     * @param pluginLayerName the name of the plugin layer being added
     * @param pluginLayer the ModuleLayer representing the plugin layer being added
     */
    void pluginLayerAdded(String pluginLayerName, ModuleLayer pluginLayer);

    /**
     * Notifies registered PluginLifecycleListeners about the removal of a plugin layer.
     *
     * @param pluginLayerName the name of the plugin layer being removed
     * @param pluginLayer the ModuleLayer representing the plugin layer being removed
     */
    void pluginLayerBeingRemoved(String pluginLayerName, ModuleLayer pluginLayer);
}
