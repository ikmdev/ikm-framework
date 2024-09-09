package dev.ikm.orchestration.provider.general.menu;

import dev.ikm.orchestration.interfaces.changeset.ChangeSetWriterService;
import dev.ikm.orchestration.interfaces.menu.MenuService;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.entity.aggregator.TemporalEntityAggregator;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * The ChangeSetMenuProvider class implements the MenuService interface to provide menu items related to generate change sets.
 */
public class ChangeSetMenuProvider implements MenuService {
    /**
     * Retrieves menu items related to generating change sets.
     *
     * @param window the window for which menu items should be retrieved
     * @return an immutable multimap of menu items, keyed by menu category
     */
    @Override
    public ImmutableMultimap<String, MenuItem> getMenuItems(Window window) {
        MutableMultimap<String, MenuItem> menuItems = Multimaps.mutable.list.empty();

        MenuItem generateChangeSetsMenuItem = new MenuItem("Generate Change Sets");
        generateChangeSetsMenuItem.setOnAction(event -> {
            long now = Instant.now().toEpochMilli();
            long yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
            TemporalEntityAggregator temporalEntityAggregator = new TemporalEntityAggregator(yesterday, now);
            TinkExecutor.threadPool().submit(() -> {
                try {
                    ChangeSetWriterService changeSetWriterService = PluggableService.first(ChangeSetWriterService.class);
                    if (changeSetWriterService.getWriteStatus() == false) {
                        changeSetWriterService.resume();
                    }
                    temporalEntityAggregator.aggregate(changeSetWriterService::write);
                    changeSetWriterService.pause();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        menuItems.put("Edit", generateChangeSetsMenuItem);

        MenuItem loadChangeSetsMenuItem = new MenuItem("Load Change Sets");
        loadChangeSetsMenuItem.setOnAction(event -> {
            Path changeSetFolder = ChangeSetWriterService.changeSetFolder();
            File[] matchingFiles = changeSetFolder.toFile().listFiles((dir, name) -> name.endsWith(".proto.zip"));
            Arrays.stream(matchingFiles).forEach((protoFile) -> {
                TinkExecutor.ioThreadPool().submit(new LoadEntitiesFromProtobufFile(protoFile));
            });
        });
        menuItems.put("Edit", loadChangeSetsMenuItem);

        return menuItems.toImmutable();
    }
}
