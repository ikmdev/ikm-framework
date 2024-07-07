package dev.ikm.orchestration.provider.changeset.writer;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.aggregator.DefaultEntityAggregator;
import dev.ikm.tinkar.entity.aggregator.EntityAggregator;
import dev.ikm.tinkar.entity.transform.EntityToTinkarSchemaTransformer;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The ChangeSetWriter class is responsible for writing Tinkar change sets to a file.
 */
public class ChangeSetWriter implements Subscriber<Integer>, AutoCloseable {

    private final EntityToTinkarSchemaTransformer entityTransformer =
            EntityToTinkarSchemaTransformer.getInstance();

    protected LongAdder conceptsAggregatedCount = new LongAdder();
    protected LongAdder semanticsAggregatedCount = new LongAdder();
    protected LongAdder patternsAggregatedCount = new LongAdder();
    protected LongAdder stampsAggregatedCount = new LongAdder();

    private final EntityService entityService;
    private final File changeSetFile;
    private final ZipOutputStream zos;
    private final Set<PublicId> moduleList = new HashSet<>();
    private final Set<PublicId> authorList = new HashSet<>();
    private final EntityAggregator entityAggregator = new DefaultEntityAggregator();

    /**
     * A class for writing change sets to a file.
     *
     * The change set writer accepts an instance of EntityService and a File object representing the change set file.
     * It creates a ZipOutputStream and a single ZIP entry named "entities.proto".
     *
     * @param entityService   the EntityService instance to use for writing the change set
     * @param changeSetFile   the File object representing the change set file to write to
     *
     * @throws IOException if an I/O error occurs while creating the change set file or the output stream
     */
    public ChangeSetWriter(EntityService entityService, File changeSetFile) throws IOException {
        this.entityService = entityService;
        this.changeSetFile = changeSetFile;
        FileOutputStream fos = new FileOutputStream(changeSetFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        this.zos = new ZipOutputStream(bos);
        // Create a single entry
        ZipEntry zipEntry = new ZipEntry("entities.proto");
        zos.putNextEntry(zipEntry);
    }

    /**
     * Process the next native identifier received by the subscriber,
     * by writing a change set corresponding to the identified component to disk.
     *
     * @param nid the integer value to process
     */
    @Override
    public void onNext(Integer nid) {
        this.entityService.getEntity(nid).ifPresent(entity -> {
            switch (entity) {
                case StampEntity stampEntity -> {
                    moduleList.add(stampEntity.module().publicId());
                    authorList.add(stampEntity.author().publicId());
                    stampsAggregatedCount.increment();
                }
                case ConceptEntity conceptEntity -> conceptsAggregatedCount.increment();
                case SemanticEntity semanticEntity -> semanticsAggregatedCount.increment();
                case PatternEntity patternEntity -> patternsAggregatedCount.increment();
                default -> throw new IllegalStateException("Unexpected value: " + entity);
            }
            TinkarMsg pbTinkarMsg = entityTransformer.transform(entity);
            try {
                pbTinkarMsg.writeDelimitedTo(zos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Closes the ChangeSetWriter.
     *
     * This method closes the ChangeSetWriter by performing the necessary cleanup steps.
     * If the entityCountSummary's total count is equal to 0, it will delete files that have no entries in them.
     * Otherwise, it will write the manifest file and perform the cleanup.
     *
     * @throws Exception if an error occurs during the close operation
     */
    @Override
    public void close() throws Exception {
        EntityCountSummary entityCountSummary = new EntityCountSummary(conceptsAggregatedCount.longValue(),
                semanticsAggregatedCount.longValue(), patternsAggregatedCount.longValue(), stampsAggregatedCount.longValue());

        if (entityCountSummary.getTotalCount() == 0) {
            // delete files that have no entries in them...
            zos.closeEntry();
            zos.finish();
            zos.close();
            changeSetFile.delete();
        } else {
            zos.closeEntry();
            // Write Manifest File
            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            zos.putNextEntry(manifestEntry);
            zos.write(generateManifestContent(entityCountSummary).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.flush();

            // Cleanup
            zos.finish();
            zos.close();
        }
    }


    /**
     * Generate the content of a manifest file based on the given entity count summary.
     *
     * @param summary the entity count summary object containing the counts of different types of entities
     * @return the content of the manifest file as a string
     */
    private String generateManifestContent(EntityCountSummary summary){
        StringBuilder manifestContent = new StringBuilder()
                // TODO: Dynamically populate this user
                .append("Packager-Name: ").append(TinkarTerm.KOMET_USER.description()).append("\n")
                .append("Package-Date: ").append(DateTimeUtil.nowWithZone()).append("\n")
                .append("Total-Count: ").append(NumberFormat.getInstance().format(summary.getTotalCount())).append("\n")
                .append("Concept-Count: ").append(NumberFormat.getInstance().format(summary.conceptsCount())).append("\n")
                .append("Semantic-Count: ").append(NumberFormat.getInstance().format(summary.semanticsCount())).append("\n")
                .append("Pattern-Count: ").append(NumberFormat.getInstance().format(summary.patternsCount())).append("\n")
                .append("Stamp-Count: ").append(NumberFormat.getInstance().format(summary.stampsCount())).append("\n")
                .append(idsToManifestEntry(moduleList))
                .append(idsToManifestEntry(authorList))
                .append("\n"); // Final new line necessary per Manifest spec

        return manifestContent.toString();
    }

    /**
     * Converts a collection of PublicIds to a manifest entry string.
     *
     * @param publicIds the collection of PublicIds to convert
     * @return the manifest entry string
     */
    private String idsToManifestEntry(Collection<PublicId> publicIds) {
        StringBuilder manifestEntry = new StringBuilder();
        publicIds.forEach((publicId) -> {
            // Convert PublicId to Manifest Entry Name
            String idString = publicId.asUuidList().stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));
            // Get Description
            Optional<Entity<EntityVersion>> entity = EntityService.get().getEntity(PrimitiveData.nid(publicId));
            String manifestDescription = "Description Undefined";
            if (entity.isPresent()) {
                manifestDescription = entity.get().description();
            }
            // Create Manifest Entry
            manifestEntry.append("\n")
                    .append("Name: ").append(idString).append("\n")
                    .append("Description: ").append(manifestDescription).append("\n");
        });
        return manifestEntry.toString();
    }
}
