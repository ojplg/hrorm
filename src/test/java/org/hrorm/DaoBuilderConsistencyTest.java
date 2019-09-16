package org.hrorm;

import org.hrorm.util.MethodWrapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DaoBuilderConsistencyTest {

    @Test
    public void testEquivalencyOfFluentMethods_DirectVersusIndirect() throws Exception {
        Class daoBuilderClass = Class.forName("org.hrorm.DaoBuilder");
        Class indirectDaoBuilderClass = Class.forName("org.hrorm.IndirectDaoBuilder");

        testEquivalencyOfFluentMethods(daoBuilderClass, indirectDaoBuilderClass, Arrays.asList("withChildSelectStrategy"));
    }

    @Test
    public void testEquivalencyOfFluentMethods_DirectVersusKeyless() throws Exception {
        Class daoBuilderClass = Class.forName("org.hrorm.DaoBuilder");
        Class keylessDaoBuilderClass = Class.forName("org.hrorm.IndirectKeylessDaoBuilder");

        testEquivalencyOfFluentMethods(daoBuilderClass, keylessDaoBuilderClass,
                Arrays.asList("withPrimaryKey", "withChildren", "withParentColumn", "withUniqueConstraint", "childSelectStrategy", "withChildSelectStrategy"));
    }

    @Test
    public void testEquivalencyOfNonFluentMethods_DirectVersusIndirect() throws Exception {
        Class daoBuilderClass = Class.forName("org.hrorm.DaoBuilder");
        Class indirectDaoBuilderClass = Class.forName("org.hrorm.IndirectDaoBuilder");

        testEquivalencyOfNonFluentMethods(indirectDaoBuilderClass, daoBuilderClass, Arrays.asList("childSelectStrategy"));
    }

    @Test
    public void testEquivalencyOfNonFluentMethods_DirectVersusKeyless() throws Exception {
        Class daoBuilderClass = Class.forName("org.hrorm.DaoBuilder");
        Class keylessDaoBuilderClass = Class.forName("org.hrorm.IndirectKeylessDaoBuilder");

        testEquivalencyOfNonFluentMethods(daoBuilderClass, keylessDaoBuilderClass,
                Arrays.asList( "buildQueries", "primaryKey", "buildDao", "hasParent", "parentColumn", "childrenDescriptors", "uniquenessConstraints", "childSelectStrategy"));
    }


    public void testEquivalencyOfNonFluentMethods(Class classA, Class classB, List<String> skippableMethods) {
        List<MethodWrapper> classAMethods = findNonFluentMethods(classA);
        List<MethodWrapper> classBMethods = findNonFluentMethods(classB);
        testMethodEquivalency(classAMethods, classBMethods, skippableMethods);
    }

    public void testEquivalencyOfFluentMethods(Class classA, Class classB, List<String> skippableMethods) {
        List<MethodWrapper> classAMethods = findFluentMethods(classA);
        List<MethodWrapper> classBMethods = findFluentMethods(classB);
        testMethodEquivalency(classAMethods, classBMethods, skippableMethods);
    }

    public void testMethodEquivalency(List<MethodWrapper> expected, List<MethodWrapper> subject, List<String> skippableMethods){
        int cnt = 0;
        for(MethodWrapper fm : expected){
            if( ! skippableMethods.contains(fm.methodName()) ) {
                boolean equivalentExists = subject.stream().anyMatch(f -> f.equivalent(fm));
                Assert.assertTrue("No match for " + fm, equivalentExists);
                cnt++;
            }
        }
        Assert.assertTrue(cnt > 10);

        List<MethodWrapper> expectedWithoutSkipped = expected.stream().filter(mw -> ! skippableMethods.contains(mw.methodName())).collect(Collectors.toList());
        List<MethodWrapper> subjectWithoutSkipped = subject.stream().filter(mw -> ! skippableMethods.contains(mw.methodName())).collect(Collectors.toList());


        Assert.assertEquals(expectedWithoutSkipped.size(), subjectWithoutSkipped.size());

    }

    public List<MethodWrapper> findFluentMethods(Class klass) {
        List<MethodWrapper> methods = MethodWrapper.fromClass(klass);
        return methods.stream().filter(MethodWrapper::isFluent).collect(Collectors.toList());
    }

    public List<MethodWrapper> findNonFluentMethods(Class klass){
        List<MethodWrapper> methods = MethodWrapper.fromClass(klass);
        return methods.stream().filter(hm -> ! hm.isFluent()).collect(Collectors.toList());
    }
}
