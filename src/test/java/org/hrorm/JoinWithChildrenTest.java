package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.join_with_children.DaoBuilders;
import org.hrorm.examples.join_with_children.Pea;
import org.hrorm.examples.join_with_children.Pod;
import org.hrorm.examples.join_with_children.Root;
import org.hrorm.examples.join_with_children.Stem;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.RandomUtils;
import org.hrorm.util.SimpleSqlFormatter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hrorm.Operator.EQUALS;

public class JoinWithChildrenTest {

    private static Helper helper = HelperFactory.forSchema("joins_with_children");

    @BeforeClass
    public static void setUpDb(){
        DaoBuilder<Pea> peaDaoBuilder = DaoBuilders.basePeaDaoBuilder();
        DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
        DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
        DaoBuilder<Root> rootDaoBuilder = DaoBuilders.baseRootDaoBuilder(stemDaoBuilder);

        Schema schema = new Schema(
                    peaDaoBuilder,
                    rootDaoBuilder,
                    stemDaoBuilder,
                    podDaoBuilder);

        String sql = schema.sql();
        helper.initializeSchemaFromSql(sql);
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @After
    public void clearTable(){
        helper.clearTables();
    }

    @Test
    public void createAndSelectWithoutStrategy(){

        Long idOne = helper.useConnection(con -> {
            Pea p1 = new Pea();
            p1.setFlag("p1");
            Pea p2 = new Pea();
            p2.setFlag("p2");
            List<Pea> ps = Arrays.asList(p1, p2);

            Pod pod = new Pod();
            pod.setMark("pod1");
            pod.setPeas(ps);

            Dao<Pod> podDao = DaoBuilders.basePodDaoBuilder().buildDao(con);
            podDao.insert(pod);

            Stem stem = new Stem();
            stem.setTag("stem1");
            stem.setPod(pod);

            Dao<Stem> dao = DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder()).buildDao(con);

            return dao.insert(stem);
        });

        Long idTwo = helper.useConnection(con -> {
            Pea p1 = new Pea();
            p1.setFlag("pp1");
            Pea p2 = new Pea();
            p2.setFlag("pp2");
            Pea p3 = new Pea();
            p3.setFlag("pp3");
            List<Pea> ps = Arrays.asList(p1, p3);

            Pod pod = new Pod();
            pod.setMark("pod2");
            pod.setPeas(ps);

            Dao<Pod> podDao = DaoBuilders.basePodDaoBuilder().buildDao(con);
            podDao.insert(pod);

            Stem stem = new Stem();
            stem.setTag("stem2");
            stem.setPod(pod);

            Dao<Stem> dao = DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder()).buildDao(con);

            return dao.insert(stem);
        });

        helper.useConnection(con -> {
            Dao<Stem> dao = DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder()).buildDao(con);
            List<Long> ids = Arrays.asList(idOne, idTwo);
            List<Stem> stems = dao.select(ids);

            Stem stem1 = stems.stream().filter(s -> s.getTag().equals("stem1")).findFirst().get();

            Assert.assertEquals("stem1", stem1.getTag());
            Assert.assertEquals("pod1", stem1.getPod().getMark());
            Assert.assertEquals(2, stem1.getPod().getPeas().size());


            Stem stem2 = stems.stream().filter(s -> s.getTag().equals("stem2")).findFirst().get();

            Assert.assertEquals("stem2", stem2.getTag());
            Assert.assertEquals("pod2", stem2.getPod().getMark());
            Assert.assertEquals(2, stem2.getPod().getPeas().size());

        });

    }


    @Test
    public void createAndSelectWithByKeysClause(){

        DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
        podDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);
        DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
        stemDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);

        Long idOne = helper.useConnection(con -> {
            Pea p1 = new Pea();
            p1.setFlag("p1");
            Pea p2 = new Pea();
            p2.setFlag("p2");
            List<Pea> ps = Arrays.asList(p1, p2);

            Pod pod = new Pod();
            pod.setMark("pod1");
            pod.setPeas(ps);

            Dao<Pod> podDao = podDaoBuilder.buildDao(con);
            podDao.insert(pod);

            Stem stem = new Stem();
            stem.setTag("stem1");
            stem.setPod(pod);

            Dao<Stem> dao = stemDaoBuilder.buildDao(con);

            return dao.insert(stem);
        });

        Long idTwo = helper.useConnection(con -> {
            Pea p1 = new Pea();
            p1.setFlag("pp1");
            Pea p2 = new Pea();
            p2.setFlag("pp2");
            Pea p3 = new Pea();
            p3.setFlag("pp3");
            List<Pea> ps = Arrays.asList(p1, p3);

            Pod pod = new Pod();
            pod.setMark("pod2");
            pod.setPeas(ps);

            Dao<Pod> podDao = podDaoBuilder.buildDao(con);
            podDao.insert(pod);

            Stem stem = new Stem();
            stem.setTag("stem2");
            stem.setPod(pod);

            Dao<Stem> dao = stemDaoBuilder.buildDao(con);

            return dao.insert(stem);
        });

        helper.useConnection(con -> {
            Dao<Stem> dao = stemDaoBuilder.buildDao(con);
            List<Long> ids = Arrays.asList(idOne, idTwo);
            List<Stem> stems = dao.select(ids);

            Assert.assertEquals(2, stems.size());

            Stem stem1 = stems.stream().filter(s -> s.getTag().equals("stem1")).findFirst().get();

            Assert.assertEquals("stem1", stem1.getTag());
            Assert.assertEquals("pod1", stem1.getPod().getMark());
            Assert.assertEquals(2, stem1.getPod().getPeas().size());

            Stem stem2 = stems.stream().filter(s -> s.getTag().equals("stem2")).findFirst().get();

            Assert.assertEquals("stem2", stem2.getTag());
            Assert.assertEquals("pod2", stem2.getPod().getMark());
            Assert.assertEquals(2, stem2.getPod().getPeas().size());
        });

    }

    @Test
    public void testRandomPeaInsertsSelectingByKeys(){
        List<Stem> stems = RandomUtils.randomNumberOf(5, 10, JoinWithChildrenTest::createRandomStemInstance);
        List<Long> stemIds = new ArrayList<>();
        for(Stem stem : stems){
            stemIds.add(insertStem(stem));
        }

        helper.useConnection(con -> {

            DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
            podDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);
            DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
            stemDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);

            Dao<Stem> stemDao = stemDaoBuilder.buildDao(con);

            List<Stem> dbStems = stemDao.select(stemIds);

            Assert.assertEquals(stems.size(), dbStems.size());

            Map<String, String> expectedPodMarks = extractPodMarks(stems);
            Map<String, List<String>> expectedPeaFlags = extractPeaFlags(stems);

            for(Stem stem : dbStems){
                Assert.assertEquals(stem.getPodMark(), expectedPodMarks.get(stem.getTag()));
                AssertHelp.sameContents(expectedPeaFlags.get(stem.getTag()), stem.getPeaFlags());
            }

        });
    }


    @Test
    public void testRandomPeaInsertsSelectingByKeys_SelectAllWorks(){
        List<Stem> stems = RandomUtils.randomNumberOf(5, 10, JoinWithChildrenTest::createRandomStemInstance);
        for(Stem stem : stems){
            insertStem(stem);
        }

        helper.useConnection(con -> {

            DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
            podDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);
            DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
            stemDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);

            Dao<Stem> stemDao = stemDaoBuilder.buildDao(con);

            List<Stem> dbStems = stemDao.select();

            Assert.assertEquals(stems.size(), dbStems.size());

            Map<String, String> expectedPodMarks = extractPodMarks(stems);
            Map<String, List<String>> expectedPeaFlags = extractPeaFlags(stems);

            for(Stem stem : dbStems){
                Assert.assertEquals(stem.getPodMark(), expectedPodMarks.get(stem.getTag()));
                AssertHelp.sameContents(expectedPeaFlags.get(stem.getTag()), stem.getPeaFlags());
            }

        });
    }


    @Test
    public void testBuildSqlForJoinedChildren(){
        SqlBuilder<Stem> sqlBuilder = new SqlBuilder<>(DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder()));

        String selectPodPrimaryKeySql = sqlBuilder.selectPrimaryKeyOfJoinedColumn(new Where("tag", EQUALS, "smurf"), "pod_id");
        String expectedSql = "select b.id from stem a left join pod b on a.pod_id = b.id where a.tag = ?";

        SimpleSqlFormatter.assertEqualSql(expectedSql, selectPodPrimaryKeySql);
    }

    @Test
    public void testRandomPeaInsertsSelectingBySubselect(){
        List<Stem> stems = RandomUtils.randomNumberOf(5, 10, JoinWithChildrenTest::createRandomStemInstance);
        List<Long> stemIds = new ArrayList<>();
        for(Stem stem : stems){
            stemIds.add(insertStem(stem));
        }

        helper.useConnection(con -> {
            DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
            podDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.SubSelectInClause);
            DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
            stemDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.SubSelectInClause);

            Dao<Stem> stemDao = stemDaoBuilder.buildDao(con);
            List<Stem> dbStems = stemDao.select(stemIds);

            Assert.assertEquals(stems.size(), dbStems.size());

            Map<String, String> expectedPodMarks = extractPodMarks(stems);
            Map<String, List<String>> expectedPeaFlags = extractPeaFlags(stems);

            for(Stem stem : dbStems){
                Assert.assertEquals(stem.getPodMark(), expectedPodMarks.get(stem.getTag()));
                AssertHelp.sameContents(expectedPeaFlags.get(stem.getTag()), stem.getPeaFlags());
            }
        });
    }

    @Test
    public void testJoinOfJoin_StandardWorks() {
        List<Stem> stems = RandomUtils.randomNumberOf(5, 10, JoinWithChildrenTest::createRandomStemInstance);
        for (Stem stem : stems) {
            insertStem(stem);
        }
        helper.useConnection(con -> {
            Dao<Root> rootDao = DaoBuilders.baseRootDaoBuilder(DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder())).buildDao(con);
            long counter = 1;
            for (Stem stem : stems) {
                Root root = new Root();
                root.setNumber(counter++);
                root.setStem(stem);
                rootDao.insert(root);
            }
        });
        helper.useConnection(con -> {
            ChildSelectStrategy childSelectStrategy = ChildSelectStrategy.Standard;
            DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
            podDaoBuilder.withChildSelectStrategy(childSelectStrategy);
            DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
            stemDaoBuilder.withChildSelectStrategy(childSelectStrategy);
            DaoBuilder<Root> rootDaoBuilder = DaoBuilders.baseRootDaoBuilder(stemDaoBuilder);
            rootDaoBuilder.withChildSelectStrategy(childSelectStrategy);

            Dao<Root> rootDao = rootDaoBuilder.buildDao(con);
            List<Root> dbRoots = rootDao.select();

            Assert.assertEquals("failed to read stems", stems.size(), dbRoots.size());

            List<Stem> dbStems = dbRoots.stream().map(Root::getStem).collect(Collectors.toList());

            Map<String, String> expectedPodMarks = extractPodMarks(stems);
            Map<String, List<String>> expectedPeaFlags = extractPeaFlags(stems);

            for (Stem stem : dbStems) {
                Assert.assertEquals("failed to load pod", stem.getPodMark(), expectedPodMarks.get(stem.getTag()));
                AssertHelp.sameContents(expectedPeaFlags.get(stem.getTag()), stem.getPeaFlags());
            }
        });
    }

    @Test
    public void testJoinOfJoin_ByKeysWorks() {
        List<Stem> stems = RandomUtils.randomNumberOf(5, 10, JoinWithChildrenTest::createRandomStemInstance);
        for (Stem stem : stems) {
            insertStem(stem);
        }
        helper.useConnection(con -> {
            Dao<Root> rootDao = DaoBuilders.baseRootDaoBuilder(DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder())).buildDao(con);
            long counter = 1;
            for (Stem stem : stems) {
                Root root = new Root();
                root.setNumber(counter++);
                root.setStem(stem);
                rootDao.insert(root);
            }
        });
        helper.useConnection(con -> {
            ChildSelectStrategy childSelectStrategy = ChildSelectStrategy.ByKeysInClause;
            DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
            podDaoBuilder.withChildSelectStrategy(childSelectStrategy);
            DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
            stemDaoBuilder.withChildSelectStrategy(childSelectStrategy);
            DaoBuilder<Root> rootDaoBuilder = DaoBuilders.baseRootDaoBuilder(stemDaoBuilder);
            rootDaoBuilder.withChildSelectStrategy(childSelectStrategy);

            Dao<Root> rootDao = rootDaoBuilder.buildDao(con);
            List<Root> dbRoots = rootDao.select();

            Assert.assertEquals("failed to read stems", stems.size(), dbRoots.size());

            List<Stem> dbStems = dbRoots.stream().map(Root::getStem).collect(Collectors.toList());

            Map<String, String> expectedPodMarks = extractPodMarks(stems);
            Map<String, List<String>> expectedPeaFlags = extractPeaFlags(stems);

            for (Stem stem : dbStems) {
                Assert.assertEquals("failed to load pod", stem.getPodMark(), expectedPodMarks.get(stem.getTag()));
                AssertHelp.sameContents(expectedPeaFlags.get(stem.getTag()), stem.getPeaFlags());
            }
        });
    }

    @Test
    public void testJoinOfJoin_SubselectWorks() {
        List<Stem> stems = RandomUtils.randomNumberOf(5, 10, JoinWithChildrenTest::createRandomStemInstance);
        for (Stem stem : stems) {
            insertStem(stem);
        }
        helper.useConnection(con -> {
            Dao<Root> rootDao = DaoBuilders.baseRootDaoBuilder(DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder())).buildDao(con);
            long counter = 1;
            for (Stem stem : stems) {
                Root root = new Root();
                root.setNumber(counter++);
                root.setStem(stem);
                rootDao.insert(root);
            }
        });
        helper.useConnection(con -> {
            ChildSelectStrategy childSelectStrategy = ChildSelectStrategy.SubSelectInClause;
            DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
            podDaoBuilder.withChildSelectStrategy(childSelectStrategy);
            DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
            stemDaoBuilder.withChildSelectStrategy(childSelectStrategy);
            DaoBuilder<Root> rootDaoBuilder = DaoBuilders.baseRootDaoBuilder(stemDaoBuilder);
            rootDaoBuilder.withChildSelectStrategy(childSelectStrategy);

            Dao<Root> rootDao = rootDaoBuilder.buildDao(con);
            List<Root> dbRoots = rootDao.select();

            Assert.assertEquals("failed to read stems", stems.size(), dbRoots.size());

            List<Stem> dbStems = dbRoots.stream().map(Root::getStem).collect(Collectors.toList());

            Map<String, String> expectedPodMarks = extractPodMarks(stems);
            Map<String, List<String>> expectedPeaFlags = extractPeaFlags(stems);

            for (Stem stem : dbStems) {
                Assert.assertEquals("failed to load pod", stem.getPodMark(), expectedPodMarks.get(stem.getTag()));
                AssertHelp.sameContents(expectedPeaFlags.get(stem.getTag()), stem.getPeaFlags());
            }
        });
    }


    @Test
    public void disallowIncompatibleChildSelectStrategies(){

        Connection connection = Mockito.mock(Connection.class);

        try {
            DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
            podDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.SubSelectInClause);
            DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);

            stemDaoBuilder.buildDao(connection);
            Assert.fail("Should not allow mixed child select strategies");
        } catch (HrormException expected){
        }
    }

    private static Map<String, String> extractPodMarks(List<Stem> stems){
        Map<String,String> podMarks = new HashMap<>();
        for(Stem stem : stems){
            podMarks.put(stem.getTag(), stem.getPodMark());
        }
        return podMarks;
    }

    private static Map<String, List<String>> extractPeaFlags(List<Stem> stems){
        Map<String,List<String>> peaFlags = new HashMap<>();
        for(Stem stem : stems){
            peaFlags.put(stem.getTag(), stem.getPeaFlags());
        }
        return peaFlags;
    }

    private static Long insertStem(Stem stem){
        return helper.useConnection(con -> {
            Dao<Pod> podDao = DaoBuilders.basePodDaoBuilder().buildDao(con);
            Dao<Stem> stemDao = DaoBuilders.baseStemDaoBuilder(DaoBuilders.basePodDaoBuilder()).buildDao(con);
            podDao.insert(stem.getPod());
            return stemDao.insert(stem);
        });
    }

    private static Stem createRandomStemInstance(){
        int numberPeas = RandomUtils.range(5,10);
        List<Pea> peas = new ArrayList<>();
        for(int i=0; i<numberPeas; i++ ){
            String flag = RandomUtils.randomAlphabeticString(5,10);
            Pea pea = new Pea();
            pea.setFlag(flag);
            peas.add(pea);
        }
        Pod pod = new Pod();
        String mark = RandomUtils.randomAlphabeticString(5,10);
        pod.setMark(mark);
        pod.setPeas(peas);
        String tag = RandomUtils.randomAlphabeticString(5,10);
        Stem stem = new Stem();
        stem.setTag(tag);
        stem.setPod(pod);
        return stem;
    }

}
