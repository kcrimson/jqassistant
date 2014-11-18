package com.buschmais.jqassistant.core.analysis.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.DefaultRuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Metric;
import com.buschmais.jqassistant.core.analysis.api.rule.MetricGroup;
import com.buschmais.jqassistant.core.analysis.api.rule.QueryTemplate;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ConstraintType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.GroupType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.IncludedReferenceType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.JqassistantRules;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.MetricGroupType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.MetricType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ParameterDefinitionType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ParameterType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ParameterTypes;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.QueryTemplateType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ReferenceType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ReferenceableType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.SeverityEnumType;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.RuleSetReader}
 * implementation.
 */
public class XmlRuleSetReader implements RuleSetReader {

    public static final String RULES_SCHEMA_LOCATION = "/META-INF/xsd/jqassistant-rules-1.0.xsd";
    public static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlRuleSetReader.class);

    private static final JAXBContext JAXB_CONTEXT;

    /**
     * Static constructor.
     */
    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public RuleSet read(List<? extends RuleSource> sources) {
        List<JqassistantRules> rules = new ArrayList<>();
        for (RuleSource ruleSource : sources) {
            if (ruleSource.isType(RuleSource.Type.XML)) {
                readXmlSource(rules, ruleSource);
            }
        }
        return convert(rules);
    }

    private void readXmlSource(List<JqassistantRules> rules, RuleSource ruleSource) {
        InputStream inputStream;
        try {
            inputStream = ruleSource.getInputStream();
        } catch (IOException e) {
            LOGGER.warn("An unexpected problem detected while opening stream for reading rules from '{}'", ruleSource.getId());
            return;
        }
        try {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            unmarshaller.setSchema(SCHEMA);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Reading rules from '{}'.", ruleSource.getId());
            }
            StreamSource streamSource = new StreamSource(inputStream);
            JAXBElement<JqassistantRules> jaxbElement = unmarshaller.unmarshal(streamSource, JqassistantRules.class);
            rules.add(jaxbElement.getValue());
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot read rules from '" + ruleSource.getId() + "'.", e);
        }
    }

    /**
     * Converts a list of {@link JqassistantRules} to a
     * {@link com.buschmais.jqassistant.core.analysis.api.rule.DefaultRuleSet}.
     *
     * @param rules
     *            The {@link JqassistantRules}.
     *
     * @return The corresponding
     *         {@link com.buschmais.jqassistant.core.analysis.api.rule.DefaultRuleSet}
     *         .
     */
    private RuleSet convert(List<JqassistantRules> rules) {
        Map<String, QueryTemplate> queryTemplates = new HashMap<>();
        Map<String, Concept> concepts = new HashMap<>();
        Map<String, Constraint> constraints = new HashMap<>();
        Map<String, Group> groups = new HashMap<>();
        Map<String, MetricGroup> metricGroups = new HashMap<>();
        for (JqassistantRules rule : rules) {
            List<ReferenceableType> queryDefinitionOrConceptOrConstraint = rule.getQueryTemplateOrConceptOrConstraint();
            for (ReferenceableType referenceableType : queryDefinitionOrConceptOrConstraint) {
                String id = referenceableType.getId();
                if (referenceableType instanceof QueryTemplateType) {
                    QueryTemplateType queryTemplateType = (QueryTemplateType) referenceableType;
                    Map<String, Class<?>> parameterTypes = new HashMap<>();
                    for (ParameterDefinitionType parameterDefinitionType : queryTemplateType.getParameterDefinition()) {
                        Class<?> parameterType;
                        switch (parameterDefinitionType.getType()) {
                        case INT:
                            parameterType = Integer.class;
                            break;
                        case STRING:
                            parameterType = String.class;
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported parameter parameterDefinitionType " + parameterDefinitionType.getType());
                        }
                        parameterTypes.put(parameterDefinitionType.getName(), parameterType);
                    }
                    QueryTemplate queryTemplate = new QueryTemplate(queryTemplateType.getCypher(), parameterTypes);
                    queryTemplates.put(id, queryTemplate);
                } else if (referenceableType instanceof ConceptType) {
                    ConceptType conceptType = (ConceptType) referenceableType;
                    String cypher = conceptType.getCypher();
                    ReferenceType useQueryTemplate = conceptType.getUseQueryTemplate();
                    String queryTemplateId = useQueryTemplate != null ? useQueryTemplate.getRefId() : null;
                    String description = conceptType.getDescription();
                    Map<String, Object> parameters = getParameterValues(conceptType.getParameter());
                    SeverityEnumType severityType = conceptType.getSeverity();
                    Severity severity = getSeverity(severityType, AbstractRule.DEFAULT_CONCEPT_SEVERITY);
                    List<ReferenceType> requiresConcept = conceptType.getRequiresConcept();
                    Set<String> requiresConcepts = getReferences(requiresConcept);
                    String deprecated = conceptType.getDeprecated();
                    Concept concept = new Concept(id, description, severity, deprecated, cypher, queryTemplateId, parameters, requiresConcepts);
                    concepts.put(id, concept);
                } else if (referenceableType instanceof ConstraintType) {
                    ConstraintType constraintType = (ConstraintType) referenceableType;
                    String cypher = constraintType.getCypher();
                    ReferenceType useQueryTemplate = constraintType.getUseQueryTemplate();
                    String queryTemplateId = useQueryTemplate != null ? useQueryTemplate.getRefId() : null;
                    String description = constraintType.getDescription();
                    Map<String, Object> parameters = getParameterValues(constraintType.getParameter());
                    SeverityEnumType severityType = constraintType.getSeverity();
                    Severity severity = getSeverity(severityType, AbstractRule.DEFAULT_CONSTRAINT_SEVERITY);
                    List<ReferenceType> requiresConcept = constraintType.getRequiresConcept();
                    Set<String> requiresConcepts = getReferences(requiresConcept);
                    String deprecated = constraintType.getDeprecated();
                    Constraint constraint = new Constraint(id, description, severity, deprecated, cypher, queryTemplateId, parameters, requiresConcepts);
                    constraints.put(id, constraint);
                } else if (referenceableType instanceof GroupType) {
                    GroupType groupType = (GroupType) referenceableType;
                    Map<String, Severity> includeConcepts = getReferences(groupType.getIncludeConcept(), AbstractRule.DEFAULT_CONCEPT_SEVERITY);
                    Map<String, Severity> includeConstraints = getReferences(groupType.getIncludeConstraint(), AbstractRule.DEFAULT_CONSTRAINT_SEVERITY);
                    Set<String> includeGroups = getReferences(groupType.getIncludeGroup());
                    Group group = new Group(id, null, includeConcepts, includeConstraints, includeGroups);
                    groups.put(id, group);
                } else if (referenceableType instanceof MetricGroupType) {
                    MetricGroupType metricGroupType = (MetricGroupType) referenceableType;
                    Map<String, Metric> metrics = new LinkedHashMap<>();
                    for (MetricType metricType : metricGroupType.getMetric()) {
                        String cypher = metricType.getCypher();
                        String description = metricType.getDescription();
                        Map<String, Class<?>> parameterTypes = getParameterTypes(metricType.getParameterDefinition());
                        Set<String> requiresConcepts = getReferences(metricType.getRequiresConcept());
                        Metric metric = new Metric(metricType.getId(), description, cypher, parameterTypes, requiresConcepts);
                        metrics.put(metricType.getId(), metric);
                    }
                    MetricGroup metricGroup = new MetricGroup(id, metricGroupType.getDescription(), metrics);
                    metricGroups.put(id, metricGroup);
                }
            }
        }
        return new DefaultRuleSet(queryTemplates, concepts, constraints, groups, metricGroups);
    }

    private Set<String> getReferences(List<? extends ReferenceType> referenceTypes) {
        Set<String> references = new HashSet<>();
        for (ReferenceType referenceType : referenceTypes) {
            references.add(referenceType.getRefId());
        }
        return references;
    }

    private Map<String, Severity> getReferences(List<IncludedReferenceType> referenceType, Severity defaultConceptSeverity) {
        Map<String, Severity> references = new HashMap<>();
        for (IncludedReferenceType includedRefereceType : referenceType) {
            Severity severity = getSeverity(includedRefereceType.getSeverity(), defaultConceptSeverity);
            references.put(includedRefereceType.getRefId(), severity);
        }
        return references;
    }

    private Map<String, Class<?>> getParameterTypes(List<ParameterDefinitionType> parameterDefinitionTypes) {
        Map<String, Class<?>> parameters = new HashMap<>();
        for (ParameterDefinitionType parameterDefinitionType : parameterDefinitionTypes) {
            Class<?> type;
            switch (parameterDefinitionType.getType()) {
            case INT:
                type = Integer.class;
                break;
            case STRING:
                type = String.class;
                break;
            default:
                throw new IllegalArgumentException("Unsupported parameter definition type" + parameterDefinitionType);
            }
            parameters.put(parameterDefinitionType.getName(), type);
        }
        return parameters;
    }

    /**
     * Get the severity.
     * 
     * @param severityType
     *            The severity type.
     * @param defaultSeverity
     *            The default severity.
     * @return The severity.
     */
    private Severity getSeverity(SeverityEnumType severityType, Severity defaultSeverity) {
        return severityType == null ? defaultSeverity : Severity.fromValue(severityType.value());
    }

    /**
     * Get a parameter value by its string representation and types.
     *
     * @param type
     *            The {@link ParameterType}.
     * @param stringValue
     *            The string representation.
     *
     * @return The parameter value.
     */
    private Object getParameterType(ParameterTypes type, String stringValue) {
        Object value;
        switch (type) {
        case INT:
            value = Integer.valueOf(stringValue);
            break;
        case STRING:
            value = stringValue;
            break;
        default:
            throw new IllegalArgumentException("Unsupported parameter types: " + type);
        }
        return value;
    }

    /**
     * Get a map of parameters.
     * 
     * @param parameter
     *            The parameters.
     * @return The map of parameters.
     */
    private Map<String, Object> getParameterValues(List<ParameterType> parameter) {
        Map<String, Object> parameters = new HashMap<>();
        for (ParameterType parameterType : parameter) {
            Object value = getParameterValue(parameterType.getType(), parameterType.getValue());
            parameters.put(parameterType.getName(), value);
        }
        return parameters;
    }

    /**
     * Get a parameter value by its string representation and types.
     *
     * @param type
     *            The {@link ParameterType}.
     * @param stringValue
     *            The string representation.
     *
     * @return The parameter value.
     */
    private Object getParameterValue(ParameterTypes type, String stringValue) {
        Object value;
        switch (type) {
        case INT:
            value = Integer.valueOf(stringValue);
            break;
        case STRING:
            value = stringValue;
            break;
        default:
            throw new IllegalArgumentException("Unsupported parameter types: " + type);
        }
        return value;
    }
}
