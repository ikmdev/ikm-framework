package dev.ikm.orchestration.provider;

import dev.ikm.orchestration.interfaces.MenuProvider;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

public class WindowServiceMenuProvider implements MenuProvider {

    @Override
    public ImmutableMultimap<String, MenuItem> getMenuItems(Window window) {
        MutableMultimap<String, MenuItem> menuItems = Multimaps.mutable.list.empty();

        MenuItem newClassicKometWindow = new MenuItem("New Classic Komet Window");
        newClassicKometWindow.setOnAction(event -> Platform.runLater(new NewClassicKometWindowTask()));
        menuItems.put("Window", newClassicKometWindow);

        return menuItems.toImmutable();
    }
}
