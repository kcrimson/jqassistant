package com.buschmais.jqassistant.scm.cli.task;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.DefaultServerImpl;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ServerTask extends AbstractJQATask {

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        Server server = new DefaultServerImpl((EmbeddedGraphStore) store, scannerPluginRepository, rulePluginRepository);
        server.start();
        getLog().info("Running server");
        getLog().info("Press <Enter> to finish.");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new CliExecutionException("Cannot read from console.", e);
        } finally {
            server.stop();
        }
    }

    @Override
    public void withOptions(CommandLine options) {
    }
}
