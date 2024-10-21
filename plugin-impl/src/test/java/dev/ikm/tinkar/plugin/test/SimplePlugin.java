package dev.ikm.tinkar.plugin.test;

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.plugin.Plugin;
import dev.ikm.tinkar.plugin.interaction.ApplicationMenuBar;
import dev.ikm.tinkar.plugin.interaction.BumpOut;
import dev.ikm.tinkar.plugin.type.Widget;

import java.util.List;
import java.util.UUID;

@Widget
@BumpOut
@ApplicationMenuBar //TODO - with menu bar are we owning interconnection of plugins (i think no)
public class SimplePlugin implements Plugin {

    private static final String PLUGIN_NAME = "SimplePlugin";
    private static final UUID PLUGIN_ID = UUID.randomUUID();

    @Override
    public UUID getId() {
        return PLUGIN_ID;
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public void reconstruct(KometPreferences kometPreferences) {

    }

    @Override
    public void register(EvtBus evtBus) {
        evtBus.subscribe(null, null, null);
    }

    @Override
    public void unregister(EvtBus evtBus) {
        evtBus.unsubscribe(null, null, null);
    }

    @Override
    public void registerTopics(EvtBus evtBus) {

    }
}
