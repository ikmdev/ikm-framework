import dev.ikm.plugin.layer.PluginLifecycleListener;
import dev.ikm.plugin.layer.internal.PluginLifecycleListenerLogger;
import dev.ikm.tinkar.common.service.PluginServiceLoader;

/**
 * The "dev.ikm.tinkar.plugin.service.boot" module is responsible for managing plugin layers and providing a logging feature for plugin lifecycles.
 * It requires two modules: "org.slf4j" and "dev.ikm.tinkar.common". It exports the "dev.ikm.plugin.layer" package.
 *
 * This module uses the PluginServiceLoader and PluginLifecycleListener service providers.
 * The PluginLifecycleListener interface is used to notify about the addition and removal of plugin layers.
 * The implementation of PluginLifecycleListener is provided by PluginLifecycleListenerLogger class, which logs events related to plugin layer addition and removal.
 */
module dev.ikm.tinkar.plugin.service.boot {
    requires org.slf4j;
    requires dev.ikm.tinkar.common;
    exports dev.ikm.plugin.layer;

    uses PluginServiceLoader;
    uses PluginLifecycleListener;

    provides PluginLifecycleListener with PluginLifecycleListenerLogger;
}