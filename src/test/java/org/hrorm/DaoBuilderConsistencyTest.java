package org.hrorm;

import org.hrorm.util.FluentMethod;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        List<FluentMethod> classAMethods = findFluentMethods(classA);
        List<FluentMethod> classBMethods = findFluentMethods(classB);

        int cnt = 0;
        for(FluentMethod fm : classAMethods){
            if( ! skippableMethods.contains(fm.methodName()) ) {
                boolean equivalentExists = classBMethods.stream().anyMatch(f -> f.equivalent(fm));
                Assert.assertTrue("No match for " + fm, equivalentExists);
                cnt++;
            }
        }
        Assert.assertTrue(cnt > 10);
        Assert.assertEquals(classAMethods.size() - skippableMethods.size(), classBMethods.size());
    }

    public List<FluentMethod> findFluentMethods(Class klass) {
        List<FluentMethod> fluentMethods = new ArrayList<>();

        for(Method method : klass.getMethods()){
            if( method.getReturnType().equals(klass)) {
                fluentMethods.add(new FluentMethod(method));
            }
        }

        return fluentMethods;
    }

}
