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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
     * This comprehensively tests foldingSelect.
     */
    @Test
    public void testFoldingSelect() {
        Connection connection = helper.connect();
        {
            long expectedSum = fakeEntities.stream()
                    .mapToLong(Keyless::getIntegerColumn)
                    .sum();

            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);
            long foldedSum = dao.foldingSelect(new Keyless(), 0L, (l, k) -> l + k.getIntegerColumn());

            Assert.assertEquals(expectedSum, foldedSum);
        }
    }

    @Test
    public void testValidate(){
        try {
            KeylessValidator.validate(helper.connect(), Keyless.DAO_BUILDER);
        } catch( HrormException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Comprehensive LIKE Operator test.
     */
    @Test
    public void likeOperatorTest() {
        Connection connection = helper.connect();
        {
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

            Map<String, Operator> columnOperatorMap = new HashMap<>();
            columnOperatorMap.put("string_column", Operator.LIKE);

            template.setStringColumn('%'+template.getStringColumn()+'%');
            List<Keyless> fromDatabase = dao.selectManyByColumns(template, columnOperatorMap);

            Assert.assertEquals(matching.size(), fromDatabase.size());
            matching.forEach(keyless -> Assert.assertTrue(fromDatabase.contains(keyless)));

        }
    }

    @Test
    public void decimalOperatorTests() {
        {
            // Build a template using a known used value.
            final Keyless template = new Keyless();
            template.setDecimalColumn(RandomUtils.randomDistinctFieldValue(fakeEntities, Keyless::getDecimalColumn));

            predicateTest(
                    template,
                    Operator.EQUALS,
                    equalTo(template, Keyless::getDecimalColumn),
                    "decimal_column");

            predicateTest(
                    template,
                    Operator.LESS_THAN,
                    lessThan(template, Keyless::getDecimalColumn),
                    "decimal_column");

            predicateTest(
                    template,
                    Operator.LESS_THAN_OR_EQUALS,
                    lessThanOrEqual(template, Keyless::getDecimalColumn),
                    "decimal_column");

            predicateTest(
                    template,
                    Operator.GREATER_THAN,
                    greaterThan(template, Keyless::getDecimalColumn),
                    "decimal_column");

            predicateTest(
                    template,
                    Operator.GREATER_THAN_OR_EQUALS,
                    greaterThanOrEqual(template, Keyless::getDecimalColumn),
                    "decimal_column");
        }
    }

    @Test
    public void integerOperatorTests() {
        {
            // Build a template using a known used value.
            final Keyless template = new Keyless();
            template.setIntegerColumn(RandomUtils.randomDistinctFieldValue(fakeEntities, Keyless::getIntegerColumn));

            predicateTest(
                    template,
                    Operator.EQUALS,
                    equalTo(template, Keyless::getIntegerColumn),
                    "integer_column");

            predicateTest(
                    template,
                    Operator.LESS_THAN,
                    lessThan(template, Keyless::getIntegerColumn),
                    "integer_column");

            predicateTest(
                    template,
                    Operator.LESS_THAN_OR_EQUALS,
                    lessThanOrEqual(template, Keyless::getIntegerColumn),
                    "integer_column");

            predicateTest(
                    template,
                    Operator.GREATER_THAN,
                    greaterThan(template, Keyless::getIntegerColumn),
                    "integer_column");

            predicateTest(
                    template,
                    Operator.GREATER_THAN_OR_EQUALS,
                    greaterThanOrEqual(template, Keyless::getIntegerColumn),
                    "integer_column");
        }
    }

    @Test
    public void timestampOperatorTests() {
        {
            // Build a template using a known used value.
            final Keyless template = new Keyless();
            template.setTimeStampColumn(RandomUtils.randomDistinctFieldValue(fakeEntities, Keyless::getTimeStampColumn));

            predicateTest(
                    template,
                    Operator.EQUALS,
                    equalTo(template, Keyless::getTimeStampColumn),
                    "timestamp_column");

            predicateTest(
                    template,
                    Operator.LESS_THAN,
                    lessThan(template, Keyless::getTimeStampColumn),
                    "timestamp_column");

            predicateTest(
                    template,
                    Operator.LESS_THAN_OR_EQUALS,
                    lessThanOrEqual(template, Keyless::getTimeStampColumn),
                    "timestamp_column");

            predicateTest(
                    template,
                    Operator.GREATER_THAN,
                    greaterThan(template, Keyless::getTimeStampColumn),
                    "timestamp_column");

            predicateTest(
                    template,
                    Operator.GREATER_THAN_OR_EQUALS,
                    greaterThanOrEqual(template, Keyless::getTimeStampColumn),
                    "timestamp_column");
        }
    }

    /**
     * Filters fakeEntities by a streamFilter Predicate equivalent to Operator, using template and column_name
     * to do the actual query.
     */
    private void predicateTest(
            Keyless template,
            Operator operator, 
            Predicate<Keyless> streamFilter,
            String columnName) {

        // DAO Setup
        Connection connection = helper.connect();
        KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

        // Filter our dataset by the predicate.
        List<Keyless> matching = fakeEntities.stream()
                .filter(streamFilter)
                .collect(Collectors.toList());

        // Setup and exec query
        Map<String, Operator> columnOperatorMap = new HashMap<>();
        columnOperatorMap.put(columnName, operator);
        List<Keyless> fromDatabase = dao.selectManyByColumns(template, columnOperatorMap);

        // Verify
        Assert.assertEquals(matching.size(), fromDatabase.size());
        matching.forEach(keyless -> Assert.assertTrue(fromDatabase.contains(keyless)));
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
        keyless.setStringColumn(RandomUtils.biname());
        keyless.setIntegerColumn(RandomUtils.range(0, 10));
        keyless.setBooleanColumn(RandomUtils.bool());
        keyless.setDecimalColumn(RandomUtils.bigDecimal()); // Probably Unique
        keyless.setTimeStampColumn(RandomUtils.localDateTime()); // Probably Unique, millisecond precision
        return keyless;
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
