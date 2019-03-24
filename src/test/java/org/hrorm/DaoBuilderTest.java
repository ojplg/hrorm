package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.util.TestLogConfig;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class DaoBuilderTest {

    static { TestLogConfig.load(); }

    private Helper helper = HelperFactory.forSchema("columns");

    @Test
    public void cannotBuildDaoWithoutPrimaryKey() throws SQLException {
        try {
            Connection connection = helper.connect();
            DaoBuilder<Object> builder = new DaoBuilder<>("", Object::new);
            builder.buildDao(connection);
            Assert.fail("Should not build Dao without primary key");
            connection.close();
        } catch (HrormException expected){
        }
    }

    @Test
    public void cannotResetPrimaryKey() {
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
