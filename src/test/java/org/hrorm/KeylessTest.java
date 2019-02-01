package org.hrorm;

import org.hrorm.examples.Keyless;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.RandomUtils;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Test operations for tables without primary keys, and KeylessDao.
 */
public class KeylessTest {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("keyless");

    // Make between 500-2000 random Keyless.
    // May seem high, but random sample behavior is better tested with a good number of entities.
    private static final List<Keyless> fakeEntities = RandomUtils.randomNumberOf(500, 2000, KeylessTest::randomKeyless);

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();

        // Insertion Phase
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        fakeEntities.forEach(dao::insert);
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    /**
     * Comprehensively tests insert/select.
     */
    @Test
    public void testInsertAndSelect(){
        Connection connection = helper.connect();

        // Make a random Keyless.
        Keyless keyless = randomKeyless();

        // No other keyless is going to have this kind of value (names based)
        keyless.setStringColumn(UUID.randomUUID().toString());

        // Insertion Phase
        {
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
            dao.insert(keyless);

            // For other tests- this is now in the database.
            fakeEntities.add(keyless);
        }
        // Test Phase
        {
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

            Keyless template = new Keyless();
            template.setStringColumn(keyless.getStringColumn());

            Keyless dbInstance = dao.selectByColumns(template, "string_column");

            Assert.assertNotNull(dbInstance);
            Assert.assertEquals(keyless.getIntegerColumn(), dbInstance.getIntegerColumn());
        }
    }

    /**
     * This comprehensively tests selectAll().
     */
    @Test
    public void testSelectAll() {
        Connection connection = helper.connect();
        {
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
            List<Keyless> allSelected = dao.selectAll();

            // We should select the same number we inserted, and every one of our generated keyless
            // should be present in the selection.
            Assert.assertEquals(fakeEntities.size(), allSelected.size());
            fakeEntities.forEach(expected -> Assert.assertTrue(allSelected.contains(expected)));
        }
    }


    /**
     * This comprehensively tests selectManyByColumns.
     */
    @Test
    public void testSelectByColumns() {
        Connection connection = helper.connect();

        {
            // We should do a sampleTest on every field type, so delegate this to a specialized method.
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
            sampleTest(dao, Keyless::getStringColumn, "string_column");
            sampleTest(dao, Keyless::getDecimalColumn, "decimal_column");
            sampleTest(dao, Keyless::getIntegerColumn, "integer_column");
            sampleTest(dao, Keyless::isBooleanColumn, "boolean_column");
            sampleTest(dao, Keyless::getTimeStampColumn, "timestamp_column");
        }
    }

    /**
     * Test that selectManyByColumns works as intended by taking a sample dataset and comparing it
     * to entities fetched from the database by the sample's field value.
      */
    private static <F> void sampleTest(KeylessDao<Keyless> dao, Function<Keyless, F> getter, String columnName) {
        // Filter the dataset by the specified field using a random, distinct value in the dataset.
        // Produces an unknown number of entities, all the with the same field value.
        List<Keyless> expectedSample = RandomUtils.randomSubsetByField(fakeEntities, getter);

        // So get one of those entities to use as the selection template.
        Keyless one = RandomUtils.randomMemberOf(expectedSample);

        // Select from the database based upon this template.
        // Should contain all entities from expectedSample
        List<Keyless> selected = dao.selectManyByColumns(one, columnName);

        // We should have the same number selected as sampled.
        Assert.assertEquals(expectedSample.size(), selected.size());

        // All the samples should match.
        expectedSample
                .forEach(expected -> Assert.assertTrue(selected.contains(expected)));
    }


    // Produces a Keyless with purely random values, designed to contain some overlap.
    public static Keyless randomKeyless() {
        Keyless keyless = new Keyless();
        keyless.setStringColumn(RandomUtils.name());
        keyless.setIntegerColumn(RandomUtils.range(0, 10));
        keyless.setBooleanColumn(RandomUtils.bool());
        keyless.setDecimalColumn(RandomUtils.bigDecimal()); // Probably Unique
        keyless.setTimeStampColumn(RandomUtils.localDateTime()); // Probably Unique, millisecond precision
        return keyless;
    }


}
