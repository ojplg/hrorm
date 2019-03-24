package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.media.ImmutableActor;
import org.hrorm.examples.media.ImmutableMediaDaoBuilders;
import org.hrorm.examples.media.ImmutableMovie;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hrorm.Where.where;

public class ImmutableMediaTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("media");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    private static final String[] MOVIE_TITLES = new String[] {
            "High Noon",
            "To Catch A Thief",
            "North By Northwest",
            "Raising Arizona",
            "Broadcast News"};

    private static final String[] ACTOR_NAMES = new String[] {
            "Holly Hunter",
            "Cary Grant",
            "Grace Kelly",
            "Albert Brooks",
            "Gary Cooper",
            "Eve Marie Saint",
            "William Hurt",
            "Nicolas Cage"
    };

    private static final String[][] ASSOCIATIONS = {
            { "High Noon", "Grace Kelly" },
            { "High Noon", "Gary Cooper" },
            { "To Catch A Thief", "Grace Kelly" },
            { "To Catch A Thief", "Cary Grant" },
            { "North By Northwest", "Cary Grant" },
            { "North By Northwest", "Eve Marie Saint" },
            { "Raising Arizona", "Holly Hunter" },
            { "Raising Arizona", "Nicolas Cage" },
            { "Broadcast News", "Holly Hunter" },
            { "Broadcast News", "William Hurt" },
            { "Broadcast News", "Albert Brooks" }
    };

    private void insertMovies() throws SQLException {
        Connection connection = helper.connect();
        Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(connection);
        for(String title : MOVIE_TITLES){
            ImmutableMovie movie = ImmutableMovie.builder().title(title).build();
            movieDao.insert(movie);
        }
        connection.commit();
        connection.close();
    }

    private void insertActors() throws SQLException {
        Connection connection = helper.connect();
        Dao<ImmutableActor> actorDao = ImmutableMediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(connection);
        for(String name : ACTOR_NAMES){
            ImmutableActor actor = ImmutableActor.builder().name(name).build();
            actorDao.insert(actor);
        }
        connection.commit();
        connection.close();
    }

    private void insertAssociations() throws SQLException {
        Connection connection = helper.connect();

        AssociationDao<ImmutableActor, ImmutableMovie> actorMovieAssociationDao =
                ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(connection);

        for( String[] pair : ASSOCIATIONS ){
            String movieName = pair[0];
            String actorName = pair[1];

            ImmutableActor actor = findActor(actorName);
            ImmutableMovie movie = findMovie(movieName);

            actorMovieAssociationDao.insertAssociation(actor, movie);
        }

        connection.commit();
        connection.close();
    }

    private ImmutableActor findActor(String name) throws SQLException {
        Connection connection = helper.connect();
        Dao<ImmutableActor> actorDao = ImmutableMediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(connection);
        ImmutableActor actor = actorDao.select(where("name", Operator.EQUALS, name)).get(0);
        connection.close();
        return actor;
    }

    private ImmutableMovie findMovie(String title) throws SQLException {
        Connection connection = helper.connect();
        Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(connection);
        ImmutableMovie movie = movieDao.select(where("title", Operator.EQUALS, title)).get(0);
        connection.close();
        return movie;
    }

    @Before
    public void doInserts() throws SQLException {
        insertActors();
        insertMovies();
        insertAssociations();
    }

    @After
    public void doDeletes(){
        helper.clearTables();
    }

    @Test
    public void testAssociationDaoSelect() throws SQLException {
        Connection connection = helper.connect();

        ImmutableActor graceKelly = findActor("Grace Kelly");

        AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(connection);
        List<ImmutableMovie> movies = associationDao.selectRightAssociates(graceKelly);

        List<String> titles = movies.stream().map(ImmutableMovie::getTitle).collect(Collectors.toList());
        AssertHelp.sameContents(new String[]{"High Noon", "To Catch A Thief"}, titles);

        connection.close();
    }

    @Test
    public void testInsertAssociation() throws SQLException {

        ImmutableActor caryGrant = findActor("Cary Grant");

        {
            Connection connection = helper.connect();
            Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(connection);
            ImmutableMovie philadelphiaStory = ImmutableMovie.builder().title("The Philadelphia Story").build();
            movieDao.insert(philadelphiaStory);
            connection.commit();
            connection.close();
        }

        {
            Connection connection = helper.connect();
            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(connection);
            ImmutableMovie philadelphiaStory = findMovie("The Philadelphia Story");
            associationDao.insertAssociation(caryGrant, philadelphiaStory);
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(connection);
            List<ImmutableMovie> movies = associationDao.selectRightAssociates(caryGrant);
            List<String> titles = movies.stream().map(ImmutableMovie::getTitle).collect(Collectors.toList());
            AssertHelp.sameContents(new String[]{"North By Northwest", "To Catch A Thief", "The Philadelphia Story"}, titles);
            connection.close();
        }
    }

    @Test
    public void testDeleteAssociation() throws SQLException {
        ImmutableActor hollyHunter = findActor("Holly Hunter");
        ImmutableMovie raisingArizona = findMovie( "Raising Arizona");

        {
            Connection connection = helper.connect();

            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(connection);
            associationDao.deleteAssociation(hollyHunter, raisingArizona);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(connection);
            List<ImmutableMovie> movies = associationDao.selectRightAssociates(hollyHunter);
            List<String> titles = movies.stream().map(ImmutableMovie::getTitle).collect(Collectors.toList());
            AssertHelp.sameContents(new String[]{"Broadcast News"}, titles);

            connection.close();
        }
    }

    @Test
    public void testAssociationDaoSelect_LeftAssociates() throws SQLException {
        Connection connection = helper.connect();
        ImmutableMovie raisingArizona = findMovie("Raising Arizona");

        AssociationDao<ImmutableActor, ImmutableMovie> associationDao =  ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(connection);
        List<ImmutableActor> actors = associationDao.selectLeftAssociates(raisingArizona);

        List<String> names = actors.stream().map(ImmutableActor::getName).collect(Collectors.toList());
        AssertHelp.sameContents(new String[]{"Holly Hunter", "Nicolas Cage"}, names);

        connection.close();
    }

}
