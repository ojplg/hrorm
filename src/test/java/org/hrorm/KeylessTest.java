package org.hrorm;

import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColor;
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
import java.util.Optional;

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

            dao.insert(keyless);
        }
        {
            KeylessDao<Keyless> dao = Keyless.DAO_BUILDER.buildDao(connection);

            Keyless template = new Keyless();
            template.setStringColumn("InsertSelectTest");

            Keyless dbInstance = dao.selectByColumns(template, "string_column");

            Assert.assertNotNull(dbInstance);
            Assert.assertEquals(762L, dbInstance.getIntegerColumn());
        }
    }


}
