package dev.ikm.orchestration.provider.sync;

import dev.ikm.orchestration.interfaces.menu.MenuService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

/**
 * The SyncServiceMenuService class implements the MenuService interface to provide
 * menu items related to Change Set synchronization.
 */
public class SyncServiceMenuService implements MenuService {

    /**
     * Retrieves the menu items for Git-based synchronization for the window. Each call creates new MenuItem instances
     * since MenuItems can be inserted into only one menu.
     *
     * @param window The window for which to generate the menu items. Each window has
     *               an observable map of properties for use by application developers,
     *               which can be leveraged in the logic of creating menu items.
     *
     * @return An ImmutableMultimap containing the menu items for synchronization.
     */
    @Override
    public ImmutableMultimap<String, MenuItem> getMenuItems(Window window) {
        // Read from preferences?

        MutableMultimap<String, MenuItem> menuItems = Multimaps.mutable.list.empty();

        MenuItem initialize = new MenuItem("Initialize");
        initialize.setOnAction(event -> TinkExecutor.threadPool().submit(new InitializeTask()));
        menuItems.put("Sync", initialize);

        MenuItem addChangesets = new MenuItem("Add Changesets");
        addChangesets.setOnAction(event -> TinkExecutor.threadPool().submit(new AddChangesetsTask()));
        menuItems.put("Sync", addChangesets);

        MenuItem push = new MenuItem("Push");
        push.setOnAction((ActionEvent event) -> TinkExecutor.threadPool().submit(new PushTask()));
        menuItems.put("Sync", push);

        MenuItem pull = new MenuItem("Pull");
        pull.setOnAction((ActionEvent event) -> TinkExecutor.threadPool().submit(new PullTask()));
        menuItems.put("Sync", pull);

        return menuItems.toImmutable();
    }
}
