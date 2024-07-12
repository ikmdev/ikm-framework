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
import dev.ikm.tinkar.common.service.PluginServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The IkmServiceLoader class represents a service that supports extensibility through plugins.
 * It provides methods to manage and access the plugins.
 * <p>
 * This class follows the Singleton design pattern to ensure that only one instance of the service exists.
 * Use the {@link #setPluginDirectory(Path)} method to initialize the service.
 * <p>
 * Use the {@link #loader(Class)} method to obtain a {@link ServiceLoader} that can be used to load plugin services
 * of a specific type.
 */
public class IkmServiceLoader implements PluginServiceLoader {
    private static final Logger LOG = LoggerFactory.getLogger(IkmServiceLoader.class);

    public static final String PATH_KEY = "dev.ikm.tinkar.plugin.service.boot.IkmServiceLoader.PATH_KEY";
    public static final String ARTIFACT_KEY = "dev.ikm.tinkar.plugin.service.boot.IkmServiceLoader.ARTIFACT_KEY";
    private static final String DefaultPluggableServiceLoaderArtifactId = "plugin-service-loader";

    private static PluginServiceLoader pluginServiceLoader;

    private final Layers layers;
    private final CopyOnWriteArraySet<ClassLoader> classLoaders = new CopyOnWriteArraySet<>();

    private static AtomicReference<IkmServiceLoader> singletonPluggableServiceManagerReference = new AtomicReference<>();
    private static AtomicReference<ClassLoader> singletonClassloaderReference = new AtomicReference<>();


    /**
     * Creates an instance of the IkmServiceLoader class.
     *
     * @param pluginsDirectories a set of PluginWatchDirectory objects representing the directories where plugins are stored
     * @throws IllegalStateException if IkmServiceLoader has already been set up
     */
    private IkmServiceLoader(Set<PluginWatchDirectory> pluginsDirectories) {
        if (IkmServiceLoader.singletonPluggableServiceManagerReference.compareAndSet(null, this) == false) {
            throw new IllegalStateException("IkmServiceLoader must only be set up once. ");
        }
        this.layers = new Layers(pluginsDirectories);
        layers.getModuleLayers().forEach(moduleLayer -> {
            moduleLayer.modules().forEach(module -> {
                classLoaders.add(module.getClassLoader());
            });
        });
        deployPluginServiceLoader(this.layers.getModuleLayers());
    }


    @Override
    public boolean ensureUses(Class<?> service) {
        return getPluginServiceLoader().ensureUses(service);
    }

    public static PluginServiceLoader getPluginServiceLoader() {
        return IkmServiceLoader.singletonPluggableServiceManagerReference.get();
    }

    public static Optional<String> findPluggableServiceLoaderJar(File dirPath, String artifactKey){
        File filesList[] = dirPath.listFiles();
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
        pluggableServiceLoaderOptional.ifPresent(serviceLoader -> IkmServiceLoader.setServiceProvider(serviceLoader));
    }

    /**
     * Sets the directory where plugins are stored.
     *
     * @param pluginDirectory the path to the directory where plugins are stored
     */
    public static void setPluginDirectory(Path pluginDirectory) {
        new IkmServiceLoader(Set.of(new PluginWatchDirectory("Standard plugins directory", pluginDirectory)));
    }

    /**
     * Sets the service provider for the IkmServiceLoader.
     *
     * @param pluginServiceLoader the PluginServiceLoader implementation used to load service providers
     */
    public static void setServiceProvider(PluginServiceLoader pluginServiceLoader) {
        IkmServiceLoader.pluginServiceLoader = pluginServiceLoader;
    }

    /**
     * Returns a ServiceLoader for the given pluggable service class.
     *
     * @param service the pluggable service class
     * @param <S>     the type of the service
     * @return a ServiceLoader object for the given service class
     */
    public <S> ServiceLoader<S> loader(Class<S> service) {
        if (IkmServiceLoader.pluginServiceLoader == null) {
            throw new IllegalStateException("PluginServiceLoader has not been set. " +
                    "Use the setServiceProvider() method to set the PluginServiceLoader.");
        }
        if (IkmServiceLoader.pluginServiceLoader.ensureUses(service)) {
            LOG.info("Adding uses {} to : IkmServiceLoader.pluginServiceLoader.", service.getName());
        }
        return IkmServiceLoader.pluginServiceLoader.loader(service);
    }

    @Override
    public Class<?> forName(String className) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            try {
                return Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException e) {
                //try again with next class loader;
            }
        }
        throw new ClassNotFoundException(className);
    }
}
