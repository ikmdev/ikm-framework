package dev.ikm.orchestration.provider.stats;

import dev.ikm.orchestration.interfaces.MenuProvider;
import dev.ikm.tinkar.common.service.TinkExecutor;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

/**
 * The StatsMenuProvider class implements the MenuProvider interface to provide statistical menu items.
 */
public class StatsMenuProvider implements MenuProvider {
    /**
     * Retrieves the menu items for a specific window.
     *
     * @param window the window for which the menu items are retrieved
     * @return an ImmutableMultimap containing the menu items for the window
     */
    @Override
    public ImmutableMultimap<String, MenuItem> getMenuItems(Window window) {

        MutableMultimap<String, MenuItem> menuItems = Multimaps.mutable.list.empty();

        MenuItem countEntitiesMenuItem = new MenuItem("Count Entities");
        countEntitiesMenuItem.setOnAction(event -> {
            TinkExecutor.threadPool().submit(new CountEntities());
        });
        menuItems.put("Stats", countEntitiesMenuItem);

        MenuItem countConceptsMenuItem = new MenuItem("Count Concepts");
        countConceptsMenuItem.setOnAction(event -> {
            TinkExecutor.threadPool().submit(new CountConcepts());
        });
        menuItems.put("Stats", countConceptsMenuItem);

        MenuItem countSemanticsMenuItem = new MenuItem("Count Semantics");
        countSemanticsMenuItem.setOnAction(event -> {
            TinkExecutor.threadPool().submit(new CountSemantics());
        });
        menuItems.put("Stats", countSemanticsMenuItem);




        return menuItems.toImmutable();
    }
}
