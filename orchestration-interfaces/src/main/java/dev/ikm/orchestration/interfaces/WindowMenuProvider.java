package dev.ikm.orchestration.interfaces;

import javafx.scene.control.Menu;
import javafx.stage.Window;

/**
 * The WindowMenuProvider interface is implemented a service that provide window menus,
 * and updates those menus as the window options change.
 */
public interface WindowMenuProvider {
    /**
     * Returns a window menu that will be managed by the menu provider.
     *
     * @param window the window for which the menu is requested
     * @return the menu that will contain information on all managed windows.
     */
    Menu getWindowMenu(Window window);
}
