import dev.ikm.orchestration.interfaces.MenuProvider;
import dev.ikm.orchestration.provider.stats.ChangeSetMenuProvider;
import dev.ikm.orchestration.provider.stats.StatsMenuProvider;

/**
 * The dev.ikm.orchestration.provider.stats module is a module that provides statistical menu items and menu items related to generating change sets.
 * This module requires the dev.ikm.orchestration.interfaces, javafx.controls, org.eclipse.collections.api, dev.ikm.tinkar.common, org.eclipse.collections, and dev.ikm.tinkar.terms
 *  modules.
 * This module provides two implementations of the MenuProvider interface: StatsMenuProvider and ChangeSetMenuProvider.
 *
 * @module dev.ikm.orchestration.provider.stats
 * @requires dev.ikm.orchestration.interfaces
 * @requires javafx.controls
 * @requires org.eclipse.collections.api
 * @requires dev.ikm.tinkar.common
 * @requires org.eclipse.collections
 * @requires dev.ikm.tinkar.terms
 * @provides MenuProvider with StatsMenuProvider, ChangeSetMenuProvider
 */
module dev.ikm.orchestration.provider.stats {
    requires dev.ikm.orchestration.interfaces;
    requires javafx.controls;
    requires org.eclipse.collections.api;
    requires dev.ikm.tinkar.common;
    requires org.eclipse.collections;
    requires dev.ikm.tinkar.terms;

    provides MenuProvider with StatsMenuProvider, ChangeSetMenuProvider;
}