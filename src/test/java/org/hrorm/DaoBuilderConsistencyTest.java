package org.hrorm;

import org.hrorm.util.HrormMethod;
import org.junit.Assert;
import org.junit.Test;

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

    public void testEquivalencyOfFluentMethods(Class classA, Class classB, List<String> skippableMethods) {

        List<HrormMethod> classAMethods = findFluentMethods(classA);
        List<HrormMethod> classBMethods = findFluentMethods(classB);

        int cnt = 0;
        for(HrormMethod fm : classAMethods){
            if( ! skippableMethods.contains(fm.methodName()) ) {
                boolean equivalentExists = classBMethods.stream().anyMatch(f -> f.equivalent(fm));
                Assert.assertTrue("No match for " + fm, equivalentExists);
                cnt++;
            }
        }
        Assert.assertTrue(cnt > 10);
        Assert.assertEquals(classAMethods.size() - skippableMethods.size(), classBMethods.size());
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
