package org.hrorm;

import org.hrorm.util.HrormMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DaoBuilderConsistencyTest {

    @Test
    public void testEquivalencyOfFluentMethods() throws Exception {
        Class indirectDaoBuilderClass = Class.forName("org.hrorm.IndirectDaoBuilder");
        Class daoBuilderClass = Class.forName("org.hrorm.DaoBuilder");
        Class keylessDaoBuilderClass = Class.forName("org.hrorm.IndirectKeylessDaoBuilder");

        testEquivalencyOfFluentMethods(indirectDaoBuilderClass, daoBuilderClass, Collections.emptyList());
        testEquivalencyOfFluentMethods(indirectDaoBuilderClass, keylessDaoBuilderClass, Collections.singletonList("withPrimaryKey"));
    }

    @Test
    public void testEquivalencyOfNonFluentMethods() throws Exception {
        Class indirectDaoBuilderClass = Class.forName("org.hrorm.IndirectDaoBuilder");
        Class daoBuilderClass = Class.forName("org.hrorm.DaoBuilder");
        Class keylessDaoBuilderClass = Class.forName("org.hrorm.IndirectKeylessDaoBuilder");

        // FIXME: This method needs to be moved to the KeylessDaoBuilder ... Ugh
        testEquivalencyOfNonFluentMethods(indirectDaoBuilderClass, daoBuilderClass, Arrays.asList("buildKeylessDao"));
        // FIXME: See above. Then extra method in KeylessDaoBuilder can be moved
//        testEquivalencyOfNonFluentMethods(indirectDaoBuilderClass, keylessDaoBuilderClass,
//                Arrays.asList("buildKeylessDao", "buildQueries", "primaryKey", "buildDao"));
    }

    public void testEquivalencyOfNonFluentMethods(Class classA, Class classB, List<String> skippableMethods) {
        List<HrormMethod> classAMethods = findNonFluentMethods(classA);
        List<HrormMethod> classBMethods = findNonFluentMethods(classB);
        testMethodEquivalency(classAMethods, classBMethods, skippableMethods);
    }

    public void testEquivalencyOfFluentMethods(Class classA, Class classB, List<String> skippableMethods) {
        List<HrormMethod> classAMethods = findFluentMethods(classA);
        List<HrormMethod> classBMethods = findFluentMethods(classB);
        testMethodEquivalency(classAMethods, classBMethods, skippableMethods);
    }

    public void testMethodEquivalency(List<HrormMethod> expected, List<HrormMethod> subject, List<String> skippableMethods){
        int cnt = 0;
        for(HrormMethod fm : expected){
            if( ! skippableMethods.contains(fm.methodName()) ) {
                boolean equivalentExists = subject.stream().anyMatch(f -> f.equivalent(fm));
                Assert.assertTrue("No match for " + fm, equivalentExists);
                cnt++;
            }
        }
        Assert.assertTrue(cnt > 10);
        Assert.assertEquals(expected.size() - skippableMethods.size(), subject.size());

    }

    public List<HrormMethod> findFluentMethods(Class klass) {
        List<HrormMethod> methods = HrormMethod.fromClass(klass);
        return methods.stream().filter(HrormMethod::isFluent).collect(Collectors.toList());
    }

    public List<HrormMethod> findNonFluentMethods(Class klass){
        List<HrormMethod> methods = HrormMethod.fromClass(klass);
        return methods.stream().filter(hm -> ! hm.isFluent()).collect(Collectors.toList());
    }
}
