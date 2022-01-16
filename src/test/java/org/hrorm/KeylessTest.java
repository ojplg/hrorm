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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hrorm.Operator.EQUALS;
import static org.hrorm.Where.where;

/**
 * Test operations for tables without primary keys, and KeylessDao.
 */
public class KeylessTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("keyless");

    // Make between 500-2000 random Keyless.
    // May seem high, but random sample behavior is better tested with a good number of entities.
    private static final List<Keyless> fakeEntities = RandomUtils.randomNumberOf(50, 200, KeylessTest::randomKeyless);

    @BeforeClass
    public static void setUpDb() throws SQLException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        helper.initializeSchema();

        // Insertion Phase
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        fakeEntities.forEach(dao::insert);
        connection.commit();
        connection.close();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    /**
     * Comprehensively tests insert/selectOne.
     */
    @Test
    public void testInsertAndSelect() throws SQLException {

        // Make a random Keyless.
        Keyless keyless = randomKeyless();

        // No other keyless is going to have this kind of value (names based)
        keyless.setStringColumn(UUID.randomUUID().toString());

        // Insertion Phase
        {
            Connection connection = helper.connect();
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
            dao.insert(keyless);

            // For other tests- this is now in the database.
            fakeEntities.add(keyless);
            connection.commit();
            connection.close();
        }
        // Test Phase
        {
            Connection connection = helper.connect();
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

            Keyless template = new Keyless();
            template.setStringColumn(keyless.getStringColumn());

            Keyless dbInstance = dao.selectOne(template, "string_column");

            Assert.assertNotNull(dbInstance);
            Assert.assertEquals(keyless.getIntegerColumn(), dbInstance.getIntegerColumn());
            connection.close();
        }
    }

    /**
     * This comprehensively tests select().
     */
    @Test
    public void testSelectAll() throws SQLException {
        Connection connection = helper.connect();

        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        List<Keyless> allSelected = dao.select();

        // We should selectOne the same number we inserted, and every one of our generated keyless
        // should be present in the selection.
        Assert.assertEquals(fakeEntities.size(), allSelected.size());
        fakeEntities.forEach(expected -> Assert.assertTrue(allSelected.contains(expected)));

        connection.close();
    }


    /**
     * This comprehensively tests select.
     */
    @Test
    public void testSelectByColumns() throws SQLException {
        Connection connection = helper.connect();
        // We should do a sampleTest on every field type, so delegate this to a specialized method.
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        sampleTest(dao, Keyless::getStringColumn, "string_column");
        sampleTest(dao, Keyless::getDecimalColumn, "fractional_column");
        sampleTest(dao, Keyless::getIntegerColumn, "integer_column");
        sampleTest(dao, Keyless::getBooleanColumn, "boolean_column");
        sampleTest(dao, Keyless::getTimeStampColumn, "timestamp_column");
        connection.close();
    }

    /**
     * This comprehensively tests foldingSelect.
     */
    @Test
    public void testFoldingSelect() throws SQLException {
        Connection connection = helper.connect();
        long expectedSum = fakeEntities.stream()
                .mapToLong(Keyless::getIntegerColumn)
                .sum();

        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
        long foldedSum = dao.foldingSelect(0L, (l, k) -> l + k.getIntegerColumn(), where());

        Assert.assertEquals(expectedSum, foldedSum);
        connection.close();
    }

    @Test
    public void testValidate() throws SQLException {
        Connection connection = helper.connect();
        KeylessValidator.validate(connection, Keyless.DAO_BUILDER);
        connection.close();
    }

    /**
     * Comprehensive LIKE Operator test.
     */
    @Test
    public void likeOperatorTest() throws SQLException {
        Connection connection = helper.connect();

        // Build a template Using a random name.
        final Keyless template = new Keyless();
        template.setStringColumn(RandomUtils.name());

        // Get all matching Keyless, case-insensitive.
        List<Keyless> matching = fakeEntities.stream()
            .filter(like(template.getStringColumn()))
            .collect(Collectors.toList());

        // Is zero results (highly unlikely) uninteresting? Remove this then...
        while (matching.isEmpty()) {
            template.setStringColumn(RandomUtils.name());
            matching = fakeEntities.stream()
                    .filter(like(template.getStringColumn()))
                    .collect(Collectors.toList());
        }

        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

        List<Keyless> fromDatabase = dao.select( where("string_column", Operator.LIKE, '%'+template.getStringColumn()+'%'));

        Assert.assertEquals(matching.size(), fromDatabase.size());
        matching.forEach(keyless -> Assert.assertTrue(fromDatabase.contains(keyless)));

        connection.close();
    }

    @Test
    public void decimalOperatorTests() throws SQLException {
        // Build a template using a known used value.
        final Keyless template = new Keyless();
        template.setDecimalColumn(RandomUtils.randomDistinctFieldValue(fakeEntities, Keyless::getDecimalColumn));

        predicateTest(
                equalTo(template, Keyless::getDecimalColumn),
                where("fractional_column", EQUALS, template.getDecimalColumn()));

        predicateTest(
                lessThan(template, Keyless::getDecimalColumn),
                where("fractional_column", Operator.LESS_THAN, template.getDecimalColumn()));

        predicateTest(
                lessThanOrEqual(template, Keyless::getDecimalColumn),
                where("fractional_column", Operator.LESS_THAN_OR_EQUALS, template.getDecimalColumn()));

        predicateTest(
                greaterThan(template, Keyless::getDecimalColumn),
                where("fractional_column", Operator.GREATER_THAN, template.getDecimalColumn()));

        predicateTest(
                greaterThanOrEqual(template, Keyless::getDecimalColumn),
                where("fractional_column", Operator.GREATER_THAN_OR_EQUALS, template.getDecimalColumn()));
    }

    @Test
    public void integerOperatorTests() throws SQLException {
        // Build a template using a known used value.
        final Keyless template = new Keyless();
        template.setIntegerColumn(RandomUtils.randomDistinctFieldValue(fakeEntities, Keyless::getIntegerColumn));

        predicateTest(
                equalTo(template, Keyless::getIntegerColumn),
                where("integer_column", EQUALS, template.getIntegerColumn()));

        predicateTest(
                lessThan(template, Keyless::getIntegerColumn),
                where("integer_column", Operator.LESS_THAN, template.getIntegerColumn()));

        predicateTest(
                lessThanOrEqual(template, Keyless::getIntegerColumn),
                where("integer_column", Operator.LESS_THAN_OR_EQUALS, template.getIntegerColumn()));

        predicateTest(
                greaterThan(template, Keyless::getIntegerColumn),
                where("integer_column", Operator.GREATER_THAN, template.getIntegerColumn()));

        predicateTest(
                greaterThanOrEqual(template, Keyless::getIntegerColumn),
                where("integer_column", Operator.GREATER_THAN_OR_EQUALS, template.getIntegerColumn()));
    }

    @Test
    public void timestampOperatorTests() throws SQLException {
        // Build a template using a known used value.
        final Keyless template = new Keyless();
        template.setTimeStampColumn(RandomUtils.randomDistinctFieldValue(fakeEntities, Keyless::getTimeStampColumn));

        predicateTest(
                equalTo(template, Keyless::getTimeStampColumn),
                where("timestamp_column", EQUALS, template.getTimeStampColumn()));

        predicateTest(
                lessThan(template, Keyless::getTimeStampColumn),
                where("timestamp_column", Operator.LESS_THAN, template.getTimeStampColumn()));

        predicateTest(
                lessThanOrEqual(template, Keyless::getTimeStampColumn),
                where("timestamp_column", Operator.LESS_THAN_OR_EQUALS, template.getTimeStampColumn()));

        predicateTest(
                greaterThan(template, Keyless::getTimeStampColumn),
                where("timestamp_column", Operator.GREATER_THAN, template.getTimeStampColumn()));

        predicateTest(
                greaterThanOrEqual(template, Keyless::getTimeStampColumn),
                where("timestamp_column", Operator.GREATER_THAN_OR_EQUALS, template.getTimeStampColumn()));
    }

    /**
     * Filters fakeEntities by a streamFilter Predicate equivalent to Operator, using template and column_name
     * to do the actual query.
     *
     * Also tests countByColumns for the same criteria.
     *
     */
    private void predicateTest(
            Predicate<Keyless> streamFilter,
            Where where) throws SQLException {

        // DAO Setup
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

        // Filter our dataset by the predicate.
        List<Keyless> matching = fakeEntities.stream()
                .filter(streamFilter)
                .collect(Collectors.toList());

        // Setup and exec query
        //Map<String, Operator> columnOperatorMap = new HashMap<>();
        //columnOperatorMap.put(columnName, operator);
        //List<Keyless> fromDatabase = dao.select(template, columnOperatorMap);

        List<Keyless> fromDatabase = dao.select(where);

        // Verify
        Assert.assertEquals("Incorrect size: ", matching.size(), fromDatabase.size());
        matching.forEach(keyless -> Assert.assertTrue(
                "Missing " + keyless + " from " + fromDatabase, fromDatabase.contains(keyless)));

        connection.close();
    }
    
    
    /**
     * Test that select works as intended by taking a sample dataset and comparing it
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
        List<Keyless> selected = dao.select(one, columnName);

        // We should have the same number selected as sampled.
        Assert.assertEquals(expectedSample.size(), selected.size());

        // All the samples should match.
        expectedSample
                .forEach(expected -> Assert.assertTrue(selected.contains(expected)));
    }


    // Produces a Keyless with purely random values, designed to contain some overlap.
    public static Keyless randomKeyless() {
        Keyless keyless = new Keyless();
        keyless.setStringColumn(RandomUtils.biname());
        keyless.setIntegerColumn((long) RandomUtils.range(0, 10));
        keyless.setBooleanColumn(RandomUtils.bool());
        keyless.setDecimalColumn(RandomUtils.bigDecimal()); // Probably Unique
        keyless.setTimeStampColumn(RandomUtils.instant()); // Probably Unique, millisecond precision
        return keyless;
    }

    @Test
    public void testSelectOneWhere() throws SQLException {
        String stringValue;
        Long integerValue;
        BigDecimal bigDecimalValue;

        {
            Connection connection = helper.connect();
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
            Keyless keyless = randomKeyless();
            stringValue = keyless.getStringColumn();
            integerValue = keyless.getIntegerColumn();
            bigDecimalValue = keyless.getDecimalColumn();

            dao.insert(keyless);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

            Keyless keyless = dao.selectOne(where("integer_column", EQUALS, integerValue)
                                .and("string_column", EQUALS, stringValue)
                                .and("fractional_column", EQUALS, bigDecimalValue));

            Assert.assertNotNull(keyless);
            connection.close();
        }
    }

    /**
     * Predicates to simulate SQL Behavior in Streams. For use with fakeEntities.
     */
    public static <F extends Comparable<F>> Predicate<Keyless> equalTo(Keyless template, final Function<Keyless, F> getter) {
        return keyless -> Objects.equals(getter.apply(template), getter.apply(keyless));
    }

    public static <F extends Comparable<F>> Predicate<Keyless> greaterThan(Keyless template, final Function<Keyless, F> getter) {
        return keyless -> getter.apply(keyless).compareTo(getter.apply(template)) > 0;
    }

    public static <F extends Comparable<F>> Predicate<Keyless> lessThan(Keyless template, final Function<Keyless, F> getter) {
        return keyless -> getter.apply(keyless).compareTo(getter.apply(template)) < 0;
    }

    public static <F extends Comparable<F>> Predicate<Keyless> greaterThanOrEqual(Keyless template, final Function<Keyless, F> getter) {
        return keyless -> getter.apply(keyless).compareTo(getter.apply(template)) >= 0;
    }

    public static <F extends Comparable<F>> Predicate<Keyless> lessThanOrEqual(Keyless template, final Function<Keyless, F> getter) {
        return keyless -> getter.apply(keyless).compareTo(getter.apply(template)) <= 0;
    }

    public static Predicate<Keyless> like(final String stringColumn) {
        return keyless -> keyless.getStringColumn().toLowerCase().contains(stringColumn.toLowerCase());
    }

}
