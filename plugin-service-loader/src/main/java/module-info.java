import dev.ikm.plugin.layer.PluginLifecycleListener;
import dev.ikm.plugin.service.loader.PluginServiceFinder;
import dev.ikm.tinkar.common.service.PluginServiceLoader;

/**
 * The dev.ikm.plugin.service.loader module is responsible for loading and managing
 * pluggable services using the Java module system.
 * It requires the dev.ikm.tinkar.plugin.service.boot, org.slf4j, and dev.ikm.tinkar.common modules.
 * It provides implementations of the PluginServiceLoader and PluginLifecycleListener interfaces.
 * It also uses the PluginLifecycleListener interface.
 */
module dev.ikm.plugin.service.loader {
    requires dev.ikm.tinkar.plugin.service.boot;
    requires org.slf4j;
    requires dev.ikm.tinkar.common;

    provides PluginServiceLoader with PluginServiceFinder;
    provides PluginLifecycleListener with PluginServiceFinder;

    uses PluginLifecycleListener;
}