package dev.ikm.orchestration.provider.general.menu;

import dev.ikm.orchestration.interfaces.menu.MenuSorterService;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Represents a provider for the MenuSorterService that maintains the contents of a Menu in sorted order.
 * The menu is maintained in sorted order even if items are added or removed from the menu.
 *
 * When a menu is registered with the MenuSorterService, the contents of each menu group in the menu
 * are sorted alphabetically, making use of a sorting HINT that is stored in the property map associated with the MenuItem.
 * A menu group is a group of components that share the same sorting HINT. HINTs are sorted according to the NaturalOrder
 * algorithm, which sorts numbers after characters, and will sort numbers containing strings by numeric value
 * (1 test, 2 test, 10 test).
 */
public class MenuSorterProvider implements MenuSorterService, ListChangeListener<MenuItem> {
    Set<Menu> weakMenuSet = Collections.newSetFromMap(new WeakHashMap<Menu, Boolean>());

    /**
     * Checks if the contents of a menu are registered for sorting.
     *
     * @param menu the menu to be checked
     * @return true if the menu is sorted, false otherwise
     */
    @Override
    public boolean isMenuRegistered(Menu menu) {
        return weakMenuSet.contains(menu);
    }

    @Override
    public void registerMenu(Menu menu) {
        weakMenuSet.add(menu);
        menu.getItems().addListener(this);
    }

    @Override
    public void unregisterMenu(Menu menu) {
        weakMenuSet.remove(menu);
        menu.getItems().removeListener(this);
    }

    @Override
    public void onChanged(Change<? extends MenuItem> c) {
        sortMenuItems(c.getList());
    }
}
