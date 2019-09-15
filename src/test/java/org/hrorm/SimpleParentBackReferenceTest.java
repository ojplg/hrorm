package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.back_reference_parents.SimpleChild;
import org.hrorm.examples.back_reference_parents.SimpleParent;
import org.hrorm.examples.back_reference_parents.SimpleParentChildDaos;
import org.hrorm.util.AssertHelp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hrorm.Operator.EQUALS;
import static org.hrorm.Where.where;

public class SimpleParentBackReferenceTest {

    private static Helper helper = HelperFactory.forSchema("simple_parents");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void testUpdateChildDirectlyWorks() {
        String parentName = "testUpdateChildDirectlyWorks";
        Long parentId = helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);

            SimpleParent simpleParent = new SimpleParent();
            simpleParent.setName(parentName);

            List<String> names = Arrays.asList("Fred", "Helen", "Archibald");
            List<SimpleChild> children = newChildren(names);

            simpleParent.setChildren(children);
            parentDao.insert(simpleParent);

            return simpleParent.getId();
        });
        helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);
            SimpleParent parent = parentDao.selectOne(parentId);

            AssertHelp.sameContents(
                    Arrays.asList("Fred", "Helen", "Archibald"),
                    parent.getChildren(),
                    SimpleChild::getName);

            SimpleChild archie = parent.getChildNamed("Archibald");
            archie.setName("Archie");

            Dao<SimpleChild> childDao = SimpleParentChildDaos.CHILD.buildDao(connection);

            childDao.update(archie);
        });
        helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);
            SimpleParent parent = parentDao.selectOne(parentId);

            AssertHelp.sameContents(
                    Arrays.asList("Fred", "Helen", "Archie"),
                    parent.getChildren(),
                    SimpleChild::getName);
        });
    }

    @Test
    public void testUpdateChildDirectlyWorksWithNquery() {
        String parentName = "testUpdateChildDirectlyWorksWithNquery";
        Long parentId = helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);

            SimpleParent simpleParent = new SimpleParent();
            simpleParent.setName(parentName);

            List<String> names = Arrays.asList("Fred", "Helen", "Archibald");
            List<SimpleChild> children = newChildren(names);

            simpleParent.setChildren(children);
            parentDao.insert(simpleParent);

            return simpleParent.getId();
        });
        helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);
            List<SimpleParent> parents = parentDao.selectNqueries(where("id", EQUALS, parentId));
            SimpleParent parent = parents.get(0);

            AssertHelp.sameContents(
                    Arrays.asList("Fred", "Helen", "Archibald"),
                    parent.getChildren(),
                    SimpleChild::getName);

            SimpleChild archie = parent.getChildNamed("Archibald");
            archie.setName("Archie");

            Dao<SimpleChild> childDao = SimpleParentChildDaos.CHILD.buildDao(connection);

            childDao.update(archie);
        });
        helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);
            List<SimpleParent> parents = parentDao.selectNqueries(where("id", EQUALS, parentId));
            SimpleParent parent = parents.get(0);

            AssertHelp.sameContents(
                    Arrays.asList("Fred", "Helen", "Archie"),
                    parent.getChildren(),
                    SimpleChild::getName);
        });
    }


    private static List<SimpleChild> newChildren(List<String> names){
        return names.stream().map(n -> newChild(n)).collect(Collectors.toList());
    }

    private static SimpleChild newChild(String name){
        SimpleChild child = new SimpleChild();
        child.setName(name);
        return child;
    }
}
