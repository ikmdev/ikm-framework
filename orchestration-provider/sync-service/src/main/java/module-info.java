import dev.ikm.orchestration.interfaces.MenuProvider;
import dev.ikm.orchestration.provider.sync.SyncServiceMenuProvider;

/**
 * This module declaration file specifies the dependencies and exports of the dev.ikm.orchestration.provider.sync module.
 * It also provides a MenuProvider implementation for synchronization-related menu items.
 *
 * Requirements:
 * - dev.ikm.orchestration.interfaces
 * - org.eclipse.jgit
 * - org.slf4j
 * - dev.ikm.tinkar.common
 * - dev.ikm.komet.preferences
 * - javafx.controls
 * - org.eclipse.collections.api
 * - org.eclipse.collections
 * - org.controlsfx.controls
 *
 * Exports:
 * - dev.ikm.orchestration.provider.sync.credential to org.controlsfx.controls, javafx.graphics
 *
 * Provides:
 * - MenuProvider with SyncServiceMenuProvider
 */
module dev.ikm.orchestration.provider.sync {
    requires dev.ikm.orchestration.interfaces;
    requires org.eclipse.jgit;
    requires org.slf4j;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.komet.preferences;
    requires javafx.controls;
    requires org.eclipse.collections.api;
    requires org.eclipse.collections;
    requires org.controlsfx.controls;

    exports dev.ikm.orchestration.provider.sync.credential to org.controlsfx.controls, javafx.graphics;

    provides MenuProvider with SyncServiceMenuProvider;
}