package com.buschmais.jqassistant.sonar.plugin.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sonar.api.rules.*;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.java.Java;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.XmlRuleSetReader;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.sonar.plugin.JQAssistant;

/**
 * The jQAssistant rule repository.
 * <p>
 * It provides two types of rules:
 * <ul>
 * <li>Rules from jQAssistant plugin descriptors which are deployed as
 * extensions.</li>
 * <li>Template rules which can be configured by the user in the UI.</li>
 * </ul>
 */
public final class JQAssistantRuleRepository extends RuleRepository {

    @SuppressWarnings("rawtypes")
    public static final Collection<Class> RULE_CLASSES = Arrays.<Class> asList(ConceptTemplateRule.class, ConstraintTemplateRule.class);

    private final AnnotationRuleParser annotationRuleParser;

    /**
     * Constructor.
     * 
     * @param annotationRuleParser
     *            The {@link AnnotationRuleParser} to use for template rules.
     */
    public JQAssistantRuleRepository(AnnotationRuleParser annotationRuleParser) {
        super(JQAssistant.KEY, Java.KEY);
        setName(JQAssistant.NAME);
        this.annotationRuleParser = annotationRuleParser;
    }

    @Override
    public List<Rule> createRules() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(JQAssistantRuleRepository.class.getClassLoader());
        RulePluginRepository rulePluginRepository;
        try {
            rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new SonarException("Cannot read rules.", e);
        }
        List<RuleSource> ruleSources = rulePluginRepository.getRuleSources();
        RuleSetReader ruleSetReader = new XmlRuleSetReader();
        RuleSet ruleSet = ruleSetReader.read(ruleSources);
        List<Rule> rules = new ArrayList<>();
        for (Concept concept : ruleSet.getConcepts().values()) {
            rules.add(createRule(concept, RuleType.Concept));
        }
        for (Constraint constraint : ruleSet.getConstraints().values()) {
            rules.add(createRule(constraint, RuleType.Constraint));
        }
        rules.addAll(annotationRuleParser.parse(JQAssistant.KEY, RULE_CLASSES));
        return rules;
    }

    /**
     * Create a rule from an executable.
     * 
     * @param executable
     *            The executable.
     * @param ruleType
     *            The rule type.
     * @return The rule.
     */
    private Rule createRule(AbstractExecutableRule executable, RuleType ruleType) {
        Rule rule = Rule.create(JQAssistant.KEY, executable.getId(), executable.getId());
        rule.setDescription(executable.getDescription());
        // set priority based on severity value
        rule.setSeverity(RulePriority.valueOf(executable.getSeverity().name()));
        StringBuilder requiresConcepts = new StringBuilder();
        for (String requiredConcept : executable.getRequiresConcepts()) {
            if (requiresConcepts.length() > 0) {
                requiresConcepts.append(",");
            }
            requiresConcepts.append(requiredConcept);
        }
        createRuleParameter(rule, RuleParameter.Type, ruleType.name());
        createRuleParameter(rule, RuleParameter.RequiresConcepts, requiresConcepts.toString());
        createRuleParameter(rule, RuleParameter.Cypher, executable.getCypher());
        return rule;
    }

    /**
     * Create a rule parameter.
     * 
     * @param rule
     *            The rule.
     * @param ruleParameterDefinition
     *            The parameter name.
     * @param value
     *            The default value.
     * @return The parameter.
     */
    private RuleParam createRuleParameter(Rule rule, RuleParameter ruleParameterDefinition, String value) {
        RuleParam parameter = rule.createParameter(ruleParameterDefinition.getName());
        parameter.setDefaultValue(value);
        return parameter;
    }
}
