package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.keyless.DaoBuilders;
import org.hrorm.examples.keyless.Sibling;
import org.hrorm.examples.keyless.UnkeyedThing;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hrorm.Operator.EQUALS;
import static org.hrorm.Where.where;

public class KeylessRelationsTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("keyless_relations");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchemaFromSql(DaoBuilders.SCHEMA.sql());
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }


    @Test
    public void testInsertAndSelect(){
        helper.useConnection(connection ->
            {
                Sibling sibling = new Sibling();
                sibling.setName("Marta");

                Dao<Sibling> siblingDao = DaoBuilders.SIBLING_DAO_BUILDER.buildDao(connection);
                siblingDao.insert(sibling);

                UnkeyedThing thing = new UnkeyedThing();
                thing.setName("Frank");
                thing.setSibling(sibling);

                KeylessDao<UnkeyedThing> unkeyedThingDao = DaoBuilders.UNKEYED_THING_DAO_BUILDER.buildDao(connection);
                unkeyedThingDao.insert(thing);
            });
        helper.useConnection(connection ->
            {
                KeylessDao<UnkeyedThing> unkeyedThingDao = DaoBuilders.UNKEYED_THING_DAO_BUILDER.buildDao(connection);
                UnkeyedThing thing = unkeyedThingDao.select(where("name", EQUALS, "Frank")).get(0);

                Assert.assertNotNull("Frank", thing.getName());
                Assert.assertEquals("Marta", thing.getSibling().getName());
            }
        );

    }
}
