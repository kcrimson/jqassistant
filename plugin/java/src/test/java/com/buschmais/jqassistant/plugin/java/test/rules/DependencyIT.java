package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.common.test.matcher.ArtifactDescriptorMatcher.artifactDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.a.A;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.packages.b.B;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types.*;

/**
 * Tests for the dependency concepts and result.
 */
public class DependencyIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "dependency:Type".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void types() throws IOException, AnalysisException {
        scanClasses(DependentType.class);
        store.beginTransaction();
        TestResult testResult = query("MATCH (t1:Type)-[:DEPENDS_ON]->(t2:Type) RETURN t2");
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(TypeAnnotation.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(TypeAnnotationValueType.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(SuperClass.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(SuperClassTypeParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(ImplementedInterface.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(ImplementedInterfaceTypeParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(FieldAnnotation.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(FieldAnnotationValueType.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(FieldType.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(FieldTypeParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodAnnotation.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodAnnotationValueType.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodAnnotation.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodReturnType.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodReturnTypeParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodAnnotation.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodParameterTypeParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(MethodException.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(LocalVariableAnnotation.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(LocalVariableAnnotationValueType.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(LocalVariable.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(LocalVariable.ReadStaticVariable.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(LocalVariable.ReadVariable.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(LocalVariable.WriteStaticVariable.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(LocalVariable.WriteVariable.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(InvokeMethodType.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(InvokeMethodType.InvokeMethodReturnType.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(InvokeMethodType.InvokeMethodReturnTypeParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(InvokeMethodType.InvokeMethodParameterType.class)));
        // assertThat(testResult.getColumn("t2"),
        // hasItem(typeDescriptor(InvokeMethodType.InvokeMethodParameterTypeTypeParameter.class)));
        assertThat(testResult.getColumn("t2"), hasItem(typeDescriptor(InvokeMethodType.InvokeMethodException.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Package".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void packages() throws IOException, AnalysisException {
        scanClassPathDirectory(getClassesDirectory(DependencyIT.class));
        applyConcept("dependency:Package");
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("package", A.class.getPackage().getName());
        assertThat(query("MATCH (p1:Package)-[:DEPENDS_ON]->(p2:Package) WHERE p1.fqn={package} RETURN p2", parameters).getColumn("p2"),
                hasItem(packageDescriptor(B.class.getPackage())));
        parameters.put("package", B.class.getPackage().getName());
        assertThat(query("MATCH (p1:Package)-[:DEPENDS_ON]->(p2:Package) WHERE p1.fqn={package} RETURN p2", parameters).getColumn("p2"),
                hasItem(packageDescriptor(A.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "dependency:Artifact".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void artifacts() throws IOException, AnalysisException {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        applyConcept("dependency:Artifact");
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("artifact", "a");
        assertThat(query("MATCH (a1:Artifact)-[:DEPENDS_ON]->(a2:Artifact) WHERE a1.fqn={artifact} RETURN a2", parameters).getColumn("a2"),
                hasItem(artifactDescriptor("b")));
        parameters.put("artifact", "b");
        assertThat(query("MATCH (a1:Artifact)-[:DEPENDS_ON]->(a2:Artifact) WHERE a1.fqn={artifact} RETURN a2", parameters).getColumn("a2"),
                hasItem(artifactDescriptor("a")));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:PackageCycles".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void packageCycles() throws IOException, AnalysisException {
        scanClasses(A.class);
        scanClasses(B.class);
        validateConstraint("dependency:PackageCycles");
        store.beginTransaction();
        Collection<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations().values();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:PackageCycles")));
        assertThat(constraintViolations, matcher);
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:TypeCycles".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void typeCycles() throws IOException, AnalysisException {
        scanClasses(A.class);
        scanClasses(B.class);
        validateConstraint("dependency:TypeCycles");
        store.beginTransaction();
        Collection<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations().values();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:TypeCycles")));
        assertThat(constraintViolations, matcher);
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "dependency:ArtifactCycles".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void artifactCycles() throws IOException, AnalysisException {
        scanClasses("a", A.class);
        scanClasses("b", B.class);
        validateConstraint("dependency:ArtifactCycles");
        store.beginTransaction();
        Collection<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations().values();
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("dependency:ArtifactCycles")));
        assertThat(constraintViolations, matcher);
        store.commitTransaction();
    }
}
