package org.hrorm;

import org.hrorm.database.H2Helper;
import org.hrorm.database.Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.Assert;
import org.junit.Test;

public class DaoBuilderTest {

    static { TestLogConfig.load(); }

    private Helper helper = new H2Helper("columns");

    @Test
    public void cannotBuildDaoWithoutPrimaryKey(){
        try {
            DaoBuilder<Object> builder = new DaoBuilder<>("", Object::new);
            builder.buildDao(helper.connect());
            Assert.fail("Should not build Dao without primary key");
        } catch (HrormException expected){
        }
    }

    @Test
    public void cannotResetPrimaryKey(){
        try {
            DaoBuilder<Object> builder = new DaoBuilder<>("", Object::new);
            builder.withPrimaryKey("foo", "sequence",
                    DaoBuilderTest::testGetter, DaoBuilderTest::testSetter);
            builder.withPrimaryKey("bar", "sequence",
                    DaoBuilderTest::testGetter, DaoBuilderTest::testSetter);
            Assert.fail("Should not allow primary key to be set twice");
        } catch (HrormException expected){
        }
    }

    private static Long testGetter(Object obj){
        return 0L;
    }

    private static void testSetter(Object obj, Long lon){
    }
}
