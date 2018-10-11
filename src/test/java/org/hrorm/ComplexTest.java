package org.hrorm;

import static org.hrorm.examples.Complex.*;

import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.util.Arrays;

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
    public void testSaveAndReadBeth(){
        long bethId;
        Boolean fredFlag;
        String gapInsignia;
        Long julesMagnitude;
        {
            Connection connection = helper.connect();

            Dao<Jules> julesDao = julesDaoBuilder.buildDao(connection);
            Dao<Ida> idaDao = idaDaoBuilder.buildDao(connection);
            Dao<Fred> fredDao = fredDaoBuilder.buildDao(connection);
            Dao<Gap> gapDao = gapDaoBuilder.buildDao(connection);
            Dao<Beth> bethDao = bethDaoBuilder.buildDao(connection);

            Jules jules = newJules();
            julesDao.insert(jules);
            julesMagnitude = jules.getMagnitude();

            Ida ida = newIda(jules);
            idaDao.insert(ida);

            Henry henry = newHenry(ida);

            Fred fred = newFred();
            fredDao.insert(fred);
            fredFlag = fred.getFlag();

            Gap gap = newGap();
            gapDao.insert(gap);
            gapInsignia = gap.getInsignia();

            Edith edith = newEdith(fred, gap);

            Don don = newDon(henry);

            Beth beth = newBeth(Arrays.asList(don), Arrays.asList(edith));
            bethId = bethDao.insert(beth);
        }
        {
            Connection connection = helper.connect();
            Dao<Beth> bethDao = bethDaoBuilder.buildDao(connection);
            Beth beth = bethDao.select(bethId);

            Assert.assertNotNull(beth);

            Don don = beth.getDons().get(0);
            Henry henry = don.getHenries().get(0);
            Jules jules = henry.getIda().getJules();

            Assert.assertEquals(julesMagnitude, jules.getMagnitude());

            Edith edith = beth.getEdiths().get(0);
            Assert.assertEquals(fredFlag, edith.getFred().getFlag());
            Assert.assertEquals(gapInsignia, edith.getGap().getInsignia());
        }
    }

}
