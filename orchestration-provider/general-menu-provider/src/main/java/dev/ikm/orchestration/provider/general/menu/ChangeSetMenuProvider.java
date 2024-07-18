package dev.ikm.orchestration.provider.general.menu;

import dev.ikm.orchestration.interfaces.changeset.ChangeSetWriterService;
import dev.ikm.orchestration.interfaces.menu.MenuService;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import java.io.IOException;

/**
 * The ChangeSetMenuProvider class implements the MenuService interface to provide menu items related to generate change sets.
 */
public class ChangeSetMenuProvider implements MenuService {
    /**
     * Retrieves menu items related to generating change sets.
     *
     * @param window the window for which menu items should be retrieved
     * @return an immutable multimap of menu items, keyed by menu category
     */
    @Override
    public ImmutableMultimap<String, MenuItem> getMenuItems(Window window) {
        MutableMultimap<String, MenuItem> menuItems = Multimaps.mutable.list.empty();

        MenuItem countEntitiesMenuItem = new MenuItem("Generate Change Sets");
        countEntitiesMenuItem.setOnAction(event -> {
            TinkExecutor.threadPool().submit(() -> {
                try {
                    ChangeSetWriterService changeSetWriterService = PluggableService.first(ChangeSetWriterService.class);
                    if (changeSetWriterService.getWriteStatus() == false) {
                        changeSetWriterService.resume();
                    }
                    changeSetWriterService.write(TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid());
                    changeSetWriterService.pause();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        menuItems.put("Edit", countEntitiesMenuItem);

        return menuItems.toImmutable();
    }
}
