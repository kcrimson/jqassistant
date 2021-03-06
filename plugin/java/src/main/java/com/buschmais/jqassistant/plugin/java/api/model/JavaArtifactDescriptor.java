package com.buschmais.jqassistant.plugin.java.api.model;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

public interface JavaArtifactDescriptor extends JavaDescriptor, ArtifactFileDescriptor {

    @ResultOf
    @Cypher("match (type:Type)<-[:CONTAINS]-(a:Artifact) where type.fqn={fqn} and id(a) in {dependencies} return type")
    Query.Result<TypeDescriptor> resolveRequiredType(@Parameter("fqn") String fqn, @Parameter("dependencies") List<? extends ArtifactDescriptor> dependencies);

    @Outgoing
    @RequiresType
    List<TypeDescriptor> getRequiresTypes();

    @Relation("REQUIRES")
    @Retention(RUNTIME)
    public @interface RequiresType {
    }

}
