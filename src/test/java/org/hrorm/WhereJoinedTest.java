package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.geography.City;
import org.hrorm.examples.geography.GeographyDaos;
import org.hrorm.examples.geography.State;
import org.hrorm.util.SimpleSqlFormatter;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.hrorm.Operator.EQUALS;
import static org.hrorm.Where.where;

public class WhereJoinedTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("geography");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void testQueries() throws SQLException {
        Connection connection = helper.connect();
        Dao<City> cityDao = GeographyDaos.CityDaoBuilder.buildDao(connection);
        Queries queries = cityDao.queries();
        String select = queries.select();
        SimpleSqlFormatter.assertEqualSql(
                "select a.id as aid, a.name as aname, "
                + "b.id as bid, b.name as bname from "
                + "city a left join state b on a.state_id = b.id"
                , select);
        connection.close();
    }

    @Test
    public void testInsertAndSelectById() throws SQLException {
        long cityId;
        {
            Connection connection = helper.connect();
            Dao<State> stateDao = GeographyDaos.StateDaoBuilder.buildDao(connection);
            State state = new State();
            state.setName("Kentucky");

            stateDao.insert(state);

            City city = new City();
            city.setName("Frankfort");
            city.setState(state);

            Dao<City> cityDao = GeographyDaos.CityDaoBuilder.buildDao(connection);

            cityId = cityDao.insert(city);
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<City> cityDao = GeographyDaos.CityDaoBuilder.buildDao(connection);

            City frankfort = cityDao.select(cityId);

            Assert.assertEquals("Kentucky", frankfort.getState().getName());
            connection.close();
        }
    }

    private long insertStateWithCities(String stateName, String ... cityNames) throws SQLException {

        Connection connection = helper.connect();

        Dao<State> stateDao = GeographyDaos.StateDaoBuilder.buildDao(connection);
        State state = new State();
        state.setName(stateName);

        long stateId = stateDao.insert(state);

        Dao<City> cityDao = GeographyDaos.CityDaoBuilder.buildDao(connection);

        for(String cityName : cityNames ) {
            City city = new City();
            city.setName(cityName);
            city.setState(state);
            cityDao.insert(city);
        }

        connection.commit();
        connection.close();

        return stateId;
    }

    @Test
    public void testSelectStateByCity() throws SQLException {

        long wisconsinId = insertStateWithCities("Wisconsin", "Milwaukee", "Madison", "Green Bay" );
        //long ohioId = insertStateWithCities("Ohio", "Cleveland", "Cincinatti", "Dayton", "Columbus" );

        Connection connection = helper.connect();

        Dao<State> stateDao = GeographyDaos.StateDaoBuilder.buildDao(connection);
        State wisconsin = stateDao.select(wisconsinId);

        List<City> wisconsinCitiesTemplateSelect;
        {
            Dao<City> cityDao = GeographyDaos.CityDaoBuilder.buildDao(connection);

            City wisconsinTemplate = new City();
            wisconsinTemplate.setState(wisconsin);

            wisconsinCitiesTemplateSelect = cityDao.selectManyByColumns(wisconsinTemplate, "state_id");

            Assert.assertEquals(3, wisconsinCitiesTemplateSelect.size());
        }

        List<City> wisconsinCitiesWhereSelect;
        {
            Dao<City> cityDao = GeographyDaos.CityDaoBuilder.buildDao(connection);

            wisconsinCitiesWhereSelect = cityDao.select(where("state_id", EQUALS, wisconsin.getId()));

            Assert.assertEquals(3, wisconsinCitiesWhereSelect.size());

        }

        Assert.assertTrue(wisconsinCitiesTemplateSelect.containsAll(wisconsinCitiesWhereSelect));

        connection.close();
    }

}
