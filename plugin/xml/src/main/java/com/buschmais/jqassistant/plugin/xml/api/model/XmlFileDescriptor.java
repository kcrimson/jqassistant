package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

public interface XmlFileDescriptor extends XmlDescriptor, FileDescriptor {

    @Relation("HAS_ROOT_ELEMENT")
    @Outgoing
    XmlElementDescriptor getRootElement();

    void setRootElement(XmlElementDescriptor rootElement);

    String getVersion();

    void setVersion(String version);

    String getCharacterEncodingScheme();

    void setCharacterEncodingScheme(String characterEncodingScheme);

    boolean isStandalone();

    void setStandalone(boolean standalone);

    boolean isWellFormed();

    void setWellFormed(boolean wellFormed);
}
