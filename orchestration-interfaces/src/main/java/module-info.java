/**
 * The module "dev.ikm.orchestration.interfaces" is a Java module that provides interfaces for orchestration functionality.
 * It requires the following JavaFX modules: javafx.graphics and javafx.controls.
 * It also requires the following third-party library: org.eclipse.collections.api.
 * The module exports the package "dev.ikm.orchestration.interfaces" to make its interfaces accessible to other modules.
 */
module dev.ikm.orchestration.interfaces {
    requires javafx.graphics;
    requires javafx.controls;
    requires dev.ikm.jpms.eclipse.collections.api;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.komet.framework;
    requires dev.ikm.komet.kview;
    exports dev.ikm.orchestration.interfaces;
    exports dev.ikm.orchestration.interfaces.changeset;
    exports dev.ikm.orchestration.interfaces.menu;
    exports dev.ikm.orchestration.interfaces.data;
    exports dev.ikm.orchestration.interfaces.journal;

}