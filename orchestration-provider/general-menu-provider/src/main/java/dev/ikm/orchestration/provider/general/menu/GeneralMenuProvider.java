package dev.ikm.orchestration.provider.general.menu;

import dev.ikm.orchestration.interfaces.menu.MenuService;
import dev.ikm.orchestration.interfaces.menu.MenuSorterService;
import dev.ikm.orchestration.interfaces.menu.WindowMenuService;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

public class GeneralMenuProvider implements MenuService {
    @Override
    public ImmutableMultimap<String, MenuItem> getMenuItems(Window window) {
        // Remove any existing quit menu...
        WindowMenuService.getMenuBar(window.getScene().getRoot()).ifPresent(menuBar -> {
            for (Menu menu : menuBar.getMenus()) {
                if (menu.getText().equals("File")) {
                    for (MenuItem item : menu.getItems()) {
                        if (item.getText().equals("Quit")) {
                            menu.getItems().remove(item);
                            break;
                        }
                    }
                }
            }
        });
        MutableMultimap<String, MenuItem> menuItems = Multimaps.mutable.list.empty();

        MenuItem quitMenuItem = new MenuItem("Quit");
        quitMenuItem.getProperties().put(MenuSorterService.SORT.HINT, "z");
        quitMenuItem.setOnAction(event -> {
           Platform.exit();
        });
        KeyCombination quitKeyCombo = new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN);
        quitMenuItem.setAccelerator(quitKeyCombo);
        menuItems.put("File", quitMenuItem);
        return menuItems.toImmutable();
    }
}
