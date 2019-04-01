package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.keyless.Keyless;
import org.hrorm.util.RandomUtils;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test operations that intentionally break HRORM in some way to ensure a good experience
 * even when screwing up.
 */
public class MischiefTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("keyless");

    private static final List<Keyless> fakeEntities = RandomUtils.randomNumberOf(50, 100, KeylessTest::randomKeyless);

    @BeforeClass
    public static void setUpDb() throws SQLException {
        helper.initializeSchema();

        // Insert using HRORM.
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        fakeEntities.forEach(dao::insert);

        // Make some intentionally bad columns directly to provoke problems.
        String sql = Stream.of("INSERT INTO keyless_table",
                "(",
                "string_column,",
                "integer_column,",
                "decimal_column,",
                "boolean_column,",
                "timestamp_column",
                ")",
                "VALUES",
                "(null, null, null, null, null)"
                ).collect(Collectors.joining(" "));
        PreparedStatement st = connection.prepareStatement(sql);
        for(int i=0;i < 100; i++) {
            st.executeUpdate();
        }
        st.close();
        connection.commit();
        connection.close();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    /**
     * See if those nulls from before cause NPEs turning them back into entities.
     * Primarily for those primitive fields where you can't insert stuff like this. In 0.6.x, you'd get NPEs
     * auto-unboxing when the setters were called.
     */
    @Test
    public void testReifyAll() throws SQLException {
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        List<Keyless> items = dao.selectAll();
        Assert.assertTrue(items.size() > 0);
        connection.close();
    }

    /**
     * These two cases are much improved from 0.6.x as-is.
     * Arguably, we should add subclasses to HrormException and then throw those during validation.
     * HrormBadColumnException, etc.
     * HrormExceptions (catch and rethrows), when found, should be considered places we could improve error handling.
     */

    @Test(expected = HrormException.class) // TODO: Validate and throw dedicated exception
    public void testIntentionalBadColumnQuery() throws SQLException {
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        dao.select(Where.where("invalid_column", Operator.EQUALS, "tamaya"));
        connection.close();
    }

    @Test(expected = HrormException.class) // TODO: Validate and throw dedicated exception
    public void testBadColumnTypeQuery() throws SQLException {
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        dao.select(Where.where("integer_column", Operator.LIKE, "kagiya"));
        connection.close();
    }

}
