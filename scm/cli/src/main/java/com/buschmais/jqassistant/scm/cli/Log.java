package com.buschmais.jqassistant.scm.cli;

import com.buschmais.jqassistant.core.analysis.api.Console;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class Log implements Console {

    private static final Log log = new Log();

    public static Log getLog() {
        return log;
    }

    public void info(final String message) {
        System.out.println(message);
    }

    public void debug(final String message) {
        info(message);
    }

    public void warn(final String message) {
        info(message);
    }

    public void error(final String message) {
        info(message);
    }
}
