package dev.ikm.tinkar.plugin;

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.List;
import java.util.UUID;


public interface Plugin {

    UUID getId();

    String getName();

    void reconstruct(KometPreferences kometPreferences);

    void register(EvtBus evtBus);

    void registerTopics(EvtBus evtBus);

    void unregister(EvtBus evtBus);

}
