package dev.ikm.orchestration.interfaces;

import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

/**
 * The WindowMenuService interface is implemented a service that provide window menus,
 * and updates those menus as the window options change.
 */
public interface WindowMenuService {
    /**
     * Adds Window menu items to the end of the provided menu bar.
     *
     * @param stage    the stage of the application
     * @param menuBar  the menu bar to add the window menu items to
     */
    void addWindowMenu(Stage stage, MenuBar menuBar);
}
