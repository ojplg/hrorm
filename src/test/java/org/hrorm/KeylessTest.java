package org.hrorm;

import org.hrorm.examples.Keyless;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test operations for tables without primary keys, and KeylessDao.
 */
public class KeylessTest {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("keyless");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void testInsertAndSelect(){
        Connection connection = helper.connect();

        {
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

            LocalDateTime time = LocalDateTime.now();

            Keyless keyless = new Keyless();
            keyless.setStringColumn("InsertSelectTest");
            keyless.setIntegerColumn(762L);
            keyless.setBooleanColumn(true);
            keyless.setDecimalColumn(new BigDecimal("4.567"));
            keyless.setTimeStampColumn(time);

            Keyless keyless2 = new Keyless();
            keyless2.setStringColumn("MultiInsertSelectTest");
            keyless2.setIntegerColumn(763L);
            keyless2.setBooleanColumn(true);
            keyless2.setDecimalColumn(new BigDecimal("4.567"));
            keyless2.setTimeStampColumn(time);

            dao.insert(keyless);
            dao.insert(keyless2);
            dao.insert(keyless2);
        }
        {
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

            Keyless template = new Keyless();
            template.setStringColumn("InsertSelectTest");

            Keyless dbInstance = dao.selectByColumns(template, "string_column");

            Assert.assertNotNull(dbInstance);
            Assert.assertEquals(762L, dbInstance.getIntegerColumn());

            Keyless template2 = new Keyless();
            template2.setStringColumn("MultiInsertSelectTest");

            List<Keyless> dbInstances = dao.selectManyByColumns(template2, "string_column");
            Assert.assertEquals(2, dbInstances.size());
            Assert.assertEquals(1, dbInstances.stream().distinct().count());
        }
    }


}
