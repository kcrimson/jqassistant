package com.buschmais.jqassistant.plugin.m2repo.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Describes a "contains" relation between {@link MavenRepositoryDescriptor} and
 * {@link RepositoryArtifactDescriptor}.
 * 
 * @author pherklotz
 */
@Relation("CONTAINS_ARTIFACT")
public interface ContainsArtifactDescriptor extends Descriptor {

    @Incoming
    RepositoryArtifactDescriptor getArtifactDescriptor();

    /**
     * The last modified date as String.
     * 
     * @return the last modified date as String.
     */
    @Property("lastModified")
    String getLastModified();

    @Outgoing
    MavenRepositoryDescriptor getMavenRepository();

    void setLastModified(String lastModified);
}
