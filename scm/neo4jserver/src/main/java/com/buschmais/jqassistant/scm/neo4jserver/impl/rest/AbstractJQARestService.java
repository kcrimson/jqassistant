package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.console.Slf4jConsole;
import com.buschmais.jqassistant.scm.common.report.ReportHelper;

public abstract class AbstractJQARestService {

    /**
     * The rules reader instance.
     */
    private RuleSet availableRules;

    private Store store = null;

    protected AbstractJQARestService(Store store) throws PluginRepositoryException {
        this.store = store;
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        RulePluginRepository rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        List<RuleSource> ruleSources = rulePluginRepository.getRuleSources();
        RuleSetReader ruleSetReader = new CompoundRuleSetReader();
        availableRules = ruleSetReader.read(ruleSources);
    }

    protected RuleSet getAvailableRules() {
        return availableRules;
    }

    protected Store getStore() {
        return store;
    }

    public InMemoryReportWriter analyze(List<String> conceptNames, List<String> constraintNames, List<String> groupNames) throws Exception {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConceptIds(conceptNames).addConstraintIds(constraintNames).addGroupIds(groupNames)
                .get();
        InMemoryReportWriter reportWriter = new InMemoryReportWriter();
        Slf4jConsole console = new Slf4jConsole();
        Analyzer analyzer = new AnalyzerImpl(store, reportWriter, console);
        analyzer.execute(getAvailableRules(), ruleSelection);
        ReportHelper reportHelper = new ReportHelper(console);
        reportHelper.verifyConceptResults(reportWriter);
        reportHelper.verifyConstraintViolations(reportWriter);
        return reportWriter;
    }
}
