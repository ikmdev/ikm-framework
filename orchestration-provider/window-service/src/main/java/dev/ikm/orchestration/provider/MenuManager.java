package dev.ikm.orchestration.provider;

import javafx.scene.control.Menu;
import javafx.stage.Window;

import java.util.concurrent.ConcurrentHashMap;

public class MenuManager {
    static final ConcurrentHashMap<Menu, Window> managedMenus = new ConcurrentHashMap<Menu, Window>();
}
