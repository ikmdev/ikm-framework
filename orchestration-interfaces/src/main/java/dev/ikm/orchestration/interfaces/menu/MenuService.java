package dev.ikm.orchestration.interfaces.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;

/**
 * The MenuService interface defines the contract for classes that provide
 * menu items for the provided window. The menus and menu items are not updated
 * once provided, they are expected to be static.
 */
public interface MenuService {
    /**
     * The provider can make use of properties on the window to customize behaviour of
     * menu items according to an agreed convention across windows and providers.
     *
     * @param window
     * @return a window-specific map of menu items.
     */

    ImmutableMultimap<String, MenuItem> getMenuItems(Window window);

    default void addMenuItems(MenuBar menuBar, Window window) {
        ImmutableMultimap<String, MenuItem> menuItems = getMenuItems(window);
        menuItems.forEachKeyValue((menuName, menuItem) -> {
            boolean found = false;
            for (Menu menu: menuBar.getMenus()) {
                if (menu.getText().equals(menuName)) {
                    menu.getItems().add(menuItem);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Menu menu = new Menu(menuName);
                menu.getItems().add(menuItem);
                menuBar.getMenus().add(menu);
            }
        });
    }

}
