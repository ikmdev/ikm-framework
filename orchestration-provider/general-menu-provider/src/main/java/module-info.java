import dev.ikm.orchestration.interfaces.menu.MenuService;
import dev.ikm.orchestration.provider.general.menu.ChangeSetMenuProvider;
import dev.ikm.orchestration.provider.general.menu.GeneralMenuProvider;

module dev.ikm.orchestration.provider.general.menu {
    requires dev.ikm.orchestration.interfaces;
    requires javafx.controls;
    requires dev.ikm.jpms.eclipse.collections.api;
    requires dev.ikm.jpms.eclipse.collections;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.terms;
    requires dev.ikm.tinkar.entity;

    opens dev.ikm.orchestration.provider.general.menu to javafx.fxml, javafx.graphics;

    provides MenuService with GeneralMenuProvider, ChangeSetMenuProvider;
}