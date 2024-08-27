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

import dev.ikm.plugin.layer.internal.PluginWatchDirectory;
import dev.ikm.plugin.layer.internal.Layers;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PluginServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The IkmServiceManager class enables provision of plugin services via jar files.
 * This manager handles loading module layers from jar files places in a specified plugins
 * directory. It provides methods to load and access the plugins.
 * <p>
 * This class follows the Singleton design pattern to ensure that only one instance of the IkmServiceManager exists.
 * Use the {@link #setPluginDirectory(Path)} method to initialize the service.
 * <p>
 * Use the {@link dev.ikm.tinkar.common.service.PluggableService#load(Class)} method to obtain a {@link ServiceLoader} that can be used to load plugin services
 * of a specific type.
 */
public class IkmServiceManager {
    private static final Logger LOG = LoggerFactory.getLogger(IkmServiceManager.class);

    public static final String PATH_KEY = "dev.ikm.tinkar.plugin.service.boot.IkmServiceManager.PATH_KEY";
    public static final String ARTIFACT_KEY = "dev.ikm.tinkar.plugin.service.boot.IkmServiceManager.ARTIFACT_KEY";
    private static final String DefaultPluggableServiceLoaderArtifactId = "plugin-service-loader";

    private final Layers layers;

    private static AtomicReference<IkmServiceManager> singletonReference = new AtomicReference<>();

    /**
     * Creates an instance of the IkmServiceManager class.
     *
     * @param pluginsDirectories a set of PluginWatchDirectory objects representing the directories where plugins are stored
     * @throws IllegalStateException if IkmServiceManager has already been set up
     */
    private IkmServiceManager(Set<PluginWatchDirectory> pluginsDirectories) {
        if (IkmServiceManager.singletonReference.compareAndSet(null, this) == false) {
            throw new IllegalStateException("IkmServiceManager must only be set up once. ");
        }
        this.layers = new Layers(pluginsDirectories);
        deployPluginServiceLoader(this.layers.getModuleLayers());
    }

    public static Optional<String> findPluggableServiceLoaderJar(File dirPath, String artifactKey){
        File[] filesList = dirPath.listFiles();
        if (filesList == null) {
            return Optional.empty();
        }
        for(File file : filesList) {
            if(file.isFile()) {
                if (file.getName().endsWith(".jar") && file.getName().startsWith(artifactKey)) {
                    return Optional.of(file.getAbsolutePath());
                }
            } else {
                Optional<String> optionalPluggableServiceLoaderJar = findPluggableServiceLoaderJar(file,artifactKey);
                if (optionalPluggableServiceLoaderJar.isPresent()) {
                    return optionalPluggableServiceLoaderJar;
                }
            }
        }
        return Optional.empty();
    }

    public static void deployPluginServiceLoader(List<ModuleLayer> parentLayers) {
        if (System.getProperty(PATH_KEY) == null) {
            String artifactKey = System.getProperty(ARTIFACT_KEY, DefaultPluggableServiceLoaderArtifactId);

            findPluggableServiceLoaderJar(new File(System.getProperty("user.dir")),
                    artifactKey).ifPresentOrElse(pluggableServiceLoaderJar -> {
                        System.setProperty(PATH_KEY, pluggableServiceLoaderJar);
                        LOG.info("Found pluggable service loader jar: {}", pluggableServiceLoaderJar);
                    },
                    () -> {throw new RuntimeException("No pluggable service loader found. \n" +
                            "Ensure that PATH_KEY and ARTIFACT_KEY system properties are provided,\n" +
                            "or that a pluggable service provider .jar file is provided at a discoverable location.\n\n"
                    );});
        }
        String pluginServiceLoaderPath = System.getProperty(PATH_KEY);

        ModuleLayer pluginServiceLoaderLayer = Layers.createModuleLayer(parentLayers,
                List.of(Path.of(pluginServiceLoaderPath)));
        ServiceLoader<PluginServiceLoader> pluggableServiceLoaderLoader =
                ServiceLoader.load(pluginServiceLoaderLayer, PluginServiceLoader.class);
        Optional<PluginServiceLoader> pluggableServiceLoaderOptional = pluggableServiceLoaderLoader.findFirst();
        pluggableServiceLoaderOptional.ifPresent(serviceLoader -> PluggableService.setPluggableServiceLoader(serviceLoader));
    }

    /**
     * Sets the directory where plugins are stored.
     *
     * @param pluginDirectory the path to the directory where plugins are stored
     */
    public static void setPluginDirectory(Path pluginDirectory) {
        new IkmServiceManager(Set.of(new PluginWatchDirectory("Standard plugins directory", pluginDirectory)));
    }

}
