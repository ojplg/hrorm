package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.immutables.DaoBuilders;
import org.hrorm.examples.immutables.ImmutableChild;
import org.hrorm.examples.immutables.ImmutableSibling;
import org.hrorm.examples.immutables.ImmutableThing;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.ListUtil;
import org.hrorm.util.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImmutableChildSelectStrategyTest {

    private static Helper HELPER = HelperFactory.forSchema("immutable_thing");

    @BeforeClass
    public static void setUpDb(){
        HELPER.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        HELPER.dropSchema();
    }

    private void runOneInsertAndSelectTest(ChildSelectStrategy childSelectStrategy){
        final String data = RandomUtils.randomAlphabeticString(5,10);
        final Instant instant = RandomUtils.instant();
        final boolean bool = RandomUtils.bool();
        final BigDecimal bigDecimal = RandomUtils.bigDecimal();

        long thingId = HELPER.useConnection(connection -> {

            Dao<ImmutableSibling> siblingDao = siblingDaoBuilder().buildDao(connection);
            Dao<ImmutableThing> thingDao = thingDaoBuilder(childSelectStrategy).buildDao(connection);

            ImmutableSibling sibling = newSibling(data);
            long id = siblingDao.insert(sibling);
            ImmutableSibling persistedSibling = siblingDao.selectOne(id);

            ImmutableChild immutableChild = newChild(
                    instant,
                    bool,
                    persistedSibling);

            ImmutableThing immutableThing = newThing(
                    bigDecimal, Collections.singletonList(immutableChild)
            );

            return thingDao.insert(immutableThing);
        });

        HELPER.useConnection(connection ->  {
            Dao<ImmutableThing> thingDao = thingDaoBuilder(childSelectStrategy).buildDao(connection);

            List<ImmutableThing> things = thingDao.select(Collections.singletonList(thingId));

            ImmutableThing immutableThing = things.get(0);

            Assert.assertEquals(bigDecimal, immutableThing.getAmount());

            ImmutableChild immutableChild = immutableThing.getChildren().get(0);

            Assert.assertEquals(instant, immutableChild.getBirthday());
            Assert.assertEquals(bool, immutableChild.getFlag());

            Assert.assertEquals(data, immutableChild.getImmutableSibling().getData());
        });
    }

    private void runMultipleInsertsAndSelectTest(ChildSelectStrategy childSelectStrategy, int parentCount){

        final Map<BigDecimal, List<String>> childStringMap = new HashMap<>();
        for(int idx=0; idx<parentCount; idx++){
            List<String> dataList = RandomUtils.randomNumberOf(5, 10, () -> RandomUtils.randomAlphabeticString(5,10));
            BigDecimal bigDecimal = RandomUtils.bigDecimal();
            childStringMap.put(bigDecimal, dataList);
        }

        List<Long> thingIdList = HELPER.useConnection(connection -> {
            Dao<ImmutableSibling> siblingDao = siblingDaoBuilder().buildDao(connection);
            Dao<ImmutableThing> thingDao = thingDaoBuilder(childSelectStrategy).buildDao(connection);

            List<Long> ids = new ArrayList<>();
            for( Map.Entry<BigDecimal, List<String>> entry : childStringMap.entrySet() ) {

                List<ImmutableChild> children = new ArrayList<>();

                for( String data : entry.getValue() ) {
                    ImmutableSibling sibling = newSibling(data);
                    long sibId = siblingDao.insert(sibling);
                    ImmutableSibling persistedSibling = siblingDao.selectOne(sibId);

                    ImmutableChild immutableChild = newChild(
                            RandomUtils.instant(),
                            RandomUtils.bool(),
                            persistedSibling);

                    children.add(immutableChild);
                }

                ImmutableThing immutableThing = newThing(
                        entry.getKey(), children
                );

                long id = thingDao.insert(immutableThing);
                ids.add(id);
            }
            return ids;
        });

        HELPER.useConnection(connection ->  {
            Dao<ImmutableThing> thingDao = thingDaoBuilder(childSelectStrategy).buildDao(connection);

            List<ImmutableThing> things = thingDao.select(thingIdList);

            Assert.assertEquals(parentCount, things.size());

            for( ImmutableThing thing : things) {
                List<String> expectedDataList = childStringMap.get(thing.getAmount());
                List<String> persistedDataList = thing.getChildren().stream().map(child -> {
                        return child.getImmutableSibling().getData();}).collect(Collectors.toList());

                AssertHelp.sameContents(expectedDataList, persistedDataList);
            }
        });
    }

    @Test
    public void testMultiInsertAndSelect_Standard(){
        runMultipleInsertsAndSelectTest(ChildSelectStrategy.Standard, RandomUtils.range(5,10));
    }

    @Test
    public void testMultiInsertAndSelect_ByKeys(){
        runMultipleInsertsAndSelectTest(ChildSelectStrategy.ByKeysInClause, RandomUtils.range(5,10));
    }

    @Test
    public void testMultiInsertAndSelect_BySubselect(){
        runMultipleInsertsAndSelectTest(ChildSelectStrategy.SubSelectInClause, RandomUtils.range(5,10));
    }


    @Test
    public void testOneInsertAndSelect_Standard(){
        runOneInsertAndSelectTest(ChildSelectStrategy.Standard);
    }

    @Test
    public void testOneInsertAndSelect_ByKeys(){
        runOneInsertAndSelectTest(ChildSelectStrategy.ByKeysInClause);
    }

    @Test
    public void testOneInsertAndSelect_SubSelect(){
        runOneInsertAndSelectTest(ChildSelectStrategy.SubSelectInClause);
    }

    private IndirectDaoBuilder<ImmutableChild, ImmutableChild.ImmutableChildBuilder> childDaoBuilder(){
        return DaoBuilders.immutableChildDaoBuilder();
    }

    private IndirectDaoBuilder<ImmutableSibling, ImmutableSibling.ImmutableSiblingBuilder> siblingDaoBuilder(){
        return DaoBuilders.immutableSiblingDaoBuilder();
    }

    private IndirectDaoBuilder<ImmutableThing, ImmutableThing.ImmutableThingBuilder> thingDaoBuilder(ChildSelectStrategy childSelectStrategy){
        return DaoBuilders.immutableObjectDaoBuilder().withChildSelectStrategy(childSelectStrategy);
    }

    private ImmutableThing newThing(BigDecimal amount, List<ImmutableChild> children){
        return ImmutableThing.builder().amount(amount).children(children).build();
    }

    private ImmutableChild newChild(Instant instant, boolean flag, ImmutableSibling sibling){
        return ImmutableChild.builder().birthday(instant).flag(flag).immutableSibling(sibling).build();
    }

    private ImmutableSibling newSibling(String data){
        return ImmutableSibling.builder().data(data).build();
    }
}
