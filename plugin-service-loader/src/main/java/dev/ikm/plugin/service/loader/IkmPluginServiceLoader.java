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

import dev.ikm.tinkar.common.service.PluginServiceLoader;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The IkmPluginServiceLoader class is responsible for loading and managing service loaders
 * for pluggable services. It implements the IkmPluginServiceLoader interface and also serves
 * as a PluginLifecycleListener to receive notifications about the addition or removal of
 * plugin layers.
 */
public class IkmPluginServiceLoader implements PluginServiceLoader {
    private static final Logger LOG = LoggerFactory.getLogger(IkmPluginServiceLoader.class);
    ReentrantLock lock = new ReentrantLock();
    private ImmutableSet<ClassLoader> classLoaders;

    /**
     * Returns a ServiceLoader for the given pluggable service class.
     * <p>
     * Note that the caller must prepare to abandon all references to the
     * loader and service if the deployment allows for dynamic removal of plugin layers,
     * as would be notified by a PluginLifecycleListener.pluginLayerBeingRemoved
     * event. If the deployment allows for dynamic removal of plugin layers,
     * then the caller must either: 1) register for the events, and act accordingly, or
     * 2) only use the services on a transient and dynamic manner on demand. Otherwise,
     * a memory leak may occur, and if an unloaded plugin layers is reloaded without
     * removing all references to the unloaded layer's classes, undefined behaviour may result.
     *
     * @param service the pluggable service class
     * @param <S>     the type of the service
     * @return a ServiceLoader object for the given service class
     */
    @Override
    public <S> ServiceLoader<S> loader(Class<S> service) {
        ensureUses(service);
        return ServiceLoader.load(IkmPluginServiceLoader.class.getModule().getLayer(), service);
    }

    /**
     * Ensures that the specified service is registered in the Java module system.
     * <p>
     * This method checks if the current class's module can use the given service.
     * If not, it adds the service to the uses clause of the module.
     *
     * @param service the service class to be checked
     * @return true if the service was added to the uses clause of the module
     * (meaning it was not already included for this module),
     * false otherwise
     */
    public boolean ensureUses(Class<?> service) {
        if (!this.getClass().getModule().canUse(service)) {
            this.getClass().getModule().addUses(service);
            return true;
        }
        return false;
    }

    @Override
    public Class<?> forName(String className) throws ClassNotFoundException {
        if (classLoaders == null) {
            lock.lock();
            try {
                if (classLoaders == null) {
                    MutableSet<ClassLoader> classLoaderSet = Sets.mutable.empty();
                    classLoaderSet.add(IkmPluginServiceLoader.class.getClassLoader());
                    for (ModuleLayer moduleLayer : this.getClass().getModule().getLayer().parents()) {
                        for (Module module : moduleLayer.modules()) {
                            if (module.getClassLoader() != null) {
                                classLoaderSet.add(module.getClassLoader());
                            }
                        }
                    }
                    classLoaders = classLoaderSet.toImmutableSet();
                }

            } finally {
                lock.unlock();
            }
        }
        for (ClassLoader classLoader : classLoaders) {
            try {
                return Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException e) {
                // Try again...
            }
        }
        throw new ClassNotFoundException(className);
    }
}
