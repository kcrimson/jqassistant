package com.buschmais.jqassistant.core.plugin.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.impl.XmlHelper;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.ObjectFactory;

/**
 * Plugin reader implementation.
 */
public class PluginConfigurationReaderImpl implements PluginConfigurationReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurationReaderImpl.class);

    private static final JAXBContext jaxbContext;

    private static final Schema schema;

    private final ClassLoader pluginClassLoader;

    private List<JqassistantPlugin> plugins = null;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
        schema = XmlHelper.getSchema(PLUGIN_SCHEMA_RESOURCE);
    }

    /**
     * Default constructor.
     */
    public PluginConfigurationReaderImpl() {
        this(PluginConfigurationReader.class.getClassLoader());
    }

    /**
     * Constructor.
     * 
     * @param pluginClassLoader
     *            The class loader to use for detecting plugins.
     */
    public PluginConfigurationReaderImpl(ClassLoader pluginClassLoader) {
        this.pluginClassLoader = pluginClassLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return pluginClassLoader;
    }

    /**
     * Read the catalogs from an {@link URL}.
     * 
     * @param pluginUrl
     *            The {@link URL}.
     * @return The {@link JqassistantPlugin}.
     */
    private JqassistantPlugin readPlugin(URL pluginUrl) {
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(pluginUrl.openStream());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open plugin stream.", e);
        }
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            return unmarshaller.unmarshal(new StreamSource(inputStream), JqassistantPlugin.class).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot read plugin from " + pluginUrl.toString(), e);
        }
    }

    /**
     * Returns an {@link Iterable} over all plugins which can be resolved from
     * the current classpath.
     * 
     * @return The plugins which can be resolved from the current classpath.
     */
    @Override
    public List<JqassistantPlugin> getPlugins() {
        if (this.plugins == null) {
            final Enumeration<URL> resources;
            try {
                resources = pluginClassLoader.getResources(PLUGIN_RESOURCE);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot get plugin resources.", e);
            }
            this.plugins = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Reading plugin descriptor from '{}'.", url);
                }
                this.plugins.add(readPlugin(url));
            }
        }
        return this.plugins;
    }
}
