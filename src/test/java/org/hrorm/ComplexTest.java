package org.hrorm;

import static org.hrorm.examples.Complex.*;

import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;

public class ComplexTest {

    private static H2Helper helper = new H2Helper("complex");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }


    @Test
    public void testCreation(){
        Connection connection = helper.connect();

        Dao<Jules> julesDao = julesDaoBuilder.buildDao(connection);
        Jules jules = newJules();
        julesDao.insert(jules);

        Assert.assertNotNull(jules.getId());
    }

}
