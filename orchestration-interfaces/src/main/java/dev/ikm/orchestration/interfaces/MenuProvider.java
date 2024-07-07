package dev.ikm.orchestration.interfaces;

import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;

/**
 * The MenuProvider interface defines the contract for classes that provide
 * menu items for a specific window.
 */
public interface MenuProvider {
    /**
     * The provider can make use of properties on the window to customize behaviour of
     * menu items according to an agreed convention across windows and providers.
     *
     * @param window
     * @return a window-specific map of menu items.
     */
    ImmutableMultimap<String, MenuItem> getMenuItems(Window window);
}
