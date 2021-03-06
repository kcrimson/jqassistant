package com.buschmais.jqassistant.core.analysis.api.rule.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A rule source which is provided from a file.
 */
public class FileRuleSource extends RuleSource {

    private File file;

    public FileRuleSource(File file) {
        this.file = file;
    }

    @Override
    protected Type getType() {
        return selectTypeById();
    }

    @Override
    public String getId() {
        return file.getAbsolutePath();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
}
