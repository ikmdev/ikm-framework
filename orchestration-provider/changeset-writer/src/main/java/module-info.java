import dev.ikm.orchestration.interfaces.ChangeSetWriterService;
import dev.ikm.orchestration.provider.changeset.writer.ChangeSetWriterProvider;

/**
 * The dev.ikm.orchestration.provider.changeset.writer module is responsible for providing the ChangeSetWriterService implementation.
 * It requires several other modules: dev.ikm.orchestration.interfaces, dev.ikm.tinkar.common, dev.ikm.tinkar.entity, dev.ikm.tinkar.schema, dev.ikm.tinkar.terms, org.eclipse.collections
 * .api.
 * The module provides the implementation class ChangeSetWriterProvider for the ChangeSetWriterService interface.
 */
module dev.ikm.orchestration.provider.changeset.writer {
    requires dev.ikm.orchestration.interfaces;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.entity;
    requires dev.ikm.tinkar.schema;
    requires dev.ikm.tinkar.terms;
    requires org.eclipse.collections.api;

    provides ChangeSetWriterService with ChangeSetWriterProvider;
}