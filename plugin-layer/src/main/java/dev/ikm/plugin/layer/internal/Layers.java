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

import dev.ikm.plugin.layer.PluggableServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Layers class represents a system of module layers used to manage plugins in an application.
 * It provides functionality for setting up and configuring the layers, deploying plugins, and handling
 * directory change events.
 */
public class Layers {
    private static final Logger LOG = LoggerFactory.getLogger(Layers.class);
    private static final Pattern PLUGIN_ARTIFACT_PATTERN = Pattern.compile("(.*?)\\-(\\d[\\d+\\-_A-Za-z\\.]*?)\\.(jar|zip|tar|tar\\.gz)");
    public static final String TINKAR_PLUGINS_TEMP_DIR = "tinkar-plugins";
    public static final String BOOT_LAYER = "boot-layer";
    public static final String PLUGIN_LAYER = "plugin-layer";

    private static final List<ModuleLayer> PLUGIN_PARENT_LAYER_AS_LIST = List.of(ModuleLayer.boot());
    /**
     * The actual module layers by name.
     */
    private final CopyOnWriteArraySet<PluginNameAndModuleLayer> moduleLayers = new CopyOnWriteArraySet<>();
    private final PluginNameAndModuleLayer bootLayer;

    /**
     * Temporary directory where all plug-ins will be copied to. Modules will be
     * sourced from there, allowing to remove plug-ins by deleting their original
     * directory.
     */
    private final Path pluginsWorkingDir;

    /**
     * All configured directories potentially containing plug-ins.
     */
    private final Set<PluginWatchDirectory> pluginsDirectories;


    private int pluginIndex = 0;

    /**
     * Creates a new instance of Layers.
     *
     * @param pluginsDirectories a set of PluginsDirectory objects representing the directories where plugins are stored
     */
    public Layers(Set<PluginWatchDirectory> pluginsDirectories) {
        this.bootLayer = new PluginNameAndModuleLayer(BOOT_LAYER, ModuleLayer.boot());
        this.moduleLayers.add(bootLayer);
        this.pluginsDirectories = Collections.unmodifiableSet(pluginsDirectories);

        try {
            this.pluginsWorkingDir = Files.createTempDirectory(TINKAR_PLUGINS_TEMP_DIR);

            if (!pluginsDirectories.isEmpty()) {
                for (PluginWatchDirectory pluginWatchDirectory : pluginsDirectories) {
                    handlePluginComponent(pluginWatchDirectory);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ModuleLayer> getModuleLayers() {
        return moduleLayers.stream().map(pluginNameAndModuleLayer -> pluginNameAndModuleLayer.moduleLayer()).toList();
    }

    /**
     * Handles the pluginWatchDirectory component by creating module layer for each pluginWatchDirectory artifact found in the directory.
     *
     * @param pluginWatchDirectory the pluginWatchDirectory object representing the pluginWatchDirectory component
     * @return a map of pluginWatchDirectory names and associated module layers
     * @throws IOException if an I/O error occurs while handling the pluginWatchDirectory component
     */
    private void handlePluginComponent(PluginWatchDirectory pluginWatchDirectory) throws IOException {
        // alternative, create a new layer out of all plugins...
        Layers.this.moduleLayers.clear();
        Layers.this.moduleLayers.add(bootLayer);
        List<Path> pluginPathEntries = getPluginPathEntries(pluginWatchDirectory);
        ModuleLayer pluginModuleLayer = createModuleLayer(PLUGIN_PARENT_LAYER_AS_LIST, pluginPathEntries);
        PluginNameAndModuleLayer pluginNameAndModuleLayer = new PluginNameAndModuleLayer(pluginWatchDirectory.name(), pluginModuleLayer);
        moduleLayers.add(pluginNameAndModuleLayer);
        // Create new service loader with new layer...
        PluggableServiceManager.deployPluginServiceLoader(moduleLayers.stream().map(pluginNameAndModuleLayerFromStream -> pluginNameAndModuleLayerFromStream.moduleLayer()).toList());
    }

    private static List<Path> getPluginPathEntries(PluginWatchDirectory pluginWatchDirectory) {
        return getPluginPathEntries(pluginWatchDirectory.directory().toFile(), new ArrayList<>());
    }
    private static List<Path> getPluginPathEntries(File directory, List<Path> pluginPathEntries) {
        for (File jarFile: directory.listFiles()){
            if (jarFile.getName().endsWith(".jar")) {
                pluginPathEntries.add(jarFile.toPath());
            } else if (jarFile.isDirectory()) {
                getPluginPathEntries(jarFile, pluginPathEntries);
            }
        }
        return pluginPathEntries;
    }

    /**
     * Computes the plugin name based on the given pluginWatchDirectory and path.
     *
     * @param pluginWatchDirectory the PluginWatchDirectory object representing the pluginWatchDirectory component
     * @param path                 the path of the plugin artifact
     * @return an Optional containing the plugin name, or an empty Optional if the plugin artifact does not match the expected pattern
     */
    private Optional<String> pluginName(PluginWatchDirectory pluginWatchDirectory, Path path) {
        Matcher matcher = PLUGIN_ARTIFACT_PATTERN.matcher(path.getFileName().toString());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String pluginArtifactId = matcher.group(1);
        String pluginVersion = matcher.group(2);
        String derivedFrom = pluginWatchDirectory.directory().getFileName().toString();
        String pluginName = String.join("-", derivedFrom, pluginArtifactId, pluginVersion);
        //return Optional.of(pluginName);
        return Optional.of(pluginArtifactId); //TODO simplifying the key for now. Assume we never try and have two versions of the same plugin.
    }

    /**
     * Creates a module layer with the given parent layers and module path entries.
     *
     * @param parentLayers      the list of parent module layers
     * @param modulePathEntries the list of module path entries
     * @return the created module layer
     */
    public static ModuleLayer createModuleLayer(List<ModuleLayer> parentLayers, List<Path> modulePathEntries) {
        ClassLoader scl = ClassLoader.getSystemClassLoader();

        ModuleFinder finder = ModuleFinder.of(modulePathEntries.toArray(Path[]::new));

        Set<String> roots = finder.findAll()
                .stream()
                .map(m -> m.descriptor().name())
                .collect(Collectors.toSet());

        Configuration appConfig = Configuration.resolve(
                finder,
                parentLayers.stream().map(ModuleLayer::configuration).collect(Collectors.toList()),
                ModuleFinder.of(),
                roots);

        return ModuleLayer.defineModulesWithOneLoader(appConfig, parentLayers, scl).layer();
    }

    /**
     * Unpacks a plugin artifact to the target directory.
     *
     * @param pluginArtifact the path of the plugin artifact to unpack
     * @param targetDir      the directory to unpack the plugin artifact to
     * @return a list containing the target directory
     * @throws UnsupportedOperationException if the plugin artifact has an unsupported file extension
     */
    private List<Path> copyPluginArtifact(Path pluginArtifact, Path targetDir) {
        String fileName = pluginArtifact.getFileName().toString();
        if (fileName.endsWith(".jar")) {
            Path dest = targetDir.resolve(fileName);
            try {
                Files.createDirectories(dest.getParent());
                Files.copy(pluginArtifact, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (fileName.endsWith(".zip")) {
            throw new UnsupportedOperationException("Can't handle .zip");
        } else if (fileName.endsWith(".tar")) {
            throw new UnsupportedOperationException("Can't handle .tar");
        } else if (fileName.endsWith(".tar.gz")) {
            throw new UnsupportedOperationException("Can't handle .tar.gz");
        }

        return List.of(targetDir);
    }

}
