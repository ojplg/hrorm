package org.hrorm;

import static org.hrorm.examples.Complex.*;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ComplexTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("complex");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void testSaveAndReadBeth() throws SQLException {
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

            Beth beth = newBeth(Collections.singletonList(don), Collections.singletonList(edith));
            bethId = bethDao.insert(beth);

            connection.commit();
            connection.close();
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

            connection.close();
        }
    }

    @Test
    public void testSaveAndReadAnn() throws SQLException {
        long annId;
        Boolean fredFlag;
        String gapInsignia;
        BigDecimal calAmount;
        Long julesMagnitude;
        {
            Connection connection = helper.connect();

            Dao<Jules> julesDao = julesDaoBuilder.buildDao(connection);
            Dao<Ida> idaDao = idaDaoBuilder.buildDao(connection);
            Dao<Fred> fredDao = fredDaoBuilder.buildDao(connection);
            Dao<Gap> gapDao = gapDaoBuilder.buildDao(connection);
            Dao<Beth> bethDao = bethDaoBuilder.buildDao(connection);
            Dao<Ann> annDao = annDaoBuilder.buildDao(connection);

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

            Beth beth = newBeth(Collections.singletonList(don), Collections.singletonList(edith));
            bethDao.insert(beth);

            Cal cal = newCal();
            calAmount = cal.getAmount();
            Ann ann = newAnn(beth, cal);

            annId = annDao.insert(ann);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Ann> annDao = annDaoBuilder.buildDao(connection);
            Ann ann = annDao.select(annId);

            Beth beth = ann.getBeth();

            Assert.assertNotNull(beth);

            List<Don> dons = beth.getDons();

            Assert.assertNotNull(dons);

            Henry henry = dons.get(0).getHenries().get(0);
            Jules jules = henry.getIda().getJules();

            Assert.assertEquals(julesMagnitude, jules.getMagnitude());

            Edith edith = beth.getEdiths().get(0);
            Assert.assertEquals(fredFlag, edith.getFred().getFlag());
            Assert.assertEquals(gapInsignia, edith.getGap().getInsignia());
            Cal cal = ann.getCals().get(0);
            Assert.assertEquals(calAmount, cal.getAmount());
            connection.close();
        }
    }

    @Test
    public void testDaoValidation() throws SQLException {
        Connection connection = helper.connect();
        Validator.validate(connection, annDaoBuilder);
        Validator.validate(connection, bethDaoBuilder);
        Validator.validate(connection, calDaoBuilder);
        Validator.validate(connection, donDaoBuilder);
        Validator.validate(connection, edithDaoBuilder);
        Validator.validate(connection, fredDaoBuilder);
        Validator.validate(connection, gapDaoBuilder);
        Validator.validate(connection, henryDaoBuilder);
        Validator.validate(connection, idaDaoBuilder);
        Validator.validate(connection, julesDaoBuilder);
        connection.close();
    }

}
