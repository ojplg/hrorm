package org.hrorm;

import org.hrorm.examples.ImmutableActor;
import org.hrorm.examples.ImmutableMediaDaoBuilders;
import org.hrorm.examples.ImmutableMovie;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hrorm.Where.where;

public class ImmutableMediaTest {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("media");

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

    private void insertMovies(){
        Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        for(String title : MOVIE_TITLES){
            ImmutableMovie movie = ImmutableMovie.builder().title(title).build();
            movieDao.insert(movie);
        }
    }

    private void insertActors(){
        Dao<ImmutableActor> actorDao = ImmutableMediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        for(String name : ACTOR_NAMES){
            ImmutableActor actor = ImmutableActor.builder().name(name).build();
            actorDao.insert(actor);
        }
    }

    private void insertAssociations(){
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
    }

    private ImmutableActor findActor(String name){
        Dao<ImmutableActor> actorDao = ImmutableMediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        return actorDao.select(where("name", Operator.EQUALS, name)).get(0);
    }

    private ImmutableMovie findMovie(String title){
        Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        return movieDao.select(where("title", Operator.EQUALS, title)).get(0);
    }

    @Before
    public void doInserts(){
        insertActors();
        insertMovies();
        insertAssociations();
    }

    @After
    public void doDeletes(){
        helper.clearTables();
    }

    @Test
    public void testAssociationDaoSelect(){
        Dao<ImmutableActor> actorDao = ImmutableMediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        ImmutableActor graceKelly = findActor("Grace Kelly");

        AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
        List<ImmutableMovie> movies = associationDao.selectRightAssociates(graceKelly);

        List<String> titles = movies.stream().map(m -> m.getTitle()).collect(Collectors.toList());
        AssertHelp.sameContents(new String[]{"High Noon", "To Catch A Thief"}, titles);
    }

    @Test
    public void testInsertAssociation(){
        Dao<ImmutableActor> actorDao = ImmutableMediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        ImmutableActor caryGrant = actorDao.select(where("name", Operator.EQUALS, "Cary Grant")).get(0);

        {
            Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
            ImmutableMovie philadelphiaStory = ImmutableMovie.builder().title("The Philadelphia Story").build();
            movieDao.insert(philadelphiaStory);
        }

        {
            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            ImmutableMovie philadelphiaStory = findMovie("The Philadelphia Story");
            associationDao.insertAssociation(caryGrant, philadelphiaStory);
        }
        {
            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            List<ImmutableMovie> movies = associationDao.selectRightAssociates(caryGrant);
            List<String> titles = movies.stream().map(m -> m.getTitle()).collect(Collectors.toList());
            AssertHelp.sameContents(new String[]{"North By Northwest", "To Catch A Thief", "The Philadelphia Story"}, titles);
        }
    }

    @Test
    public void testDeleteAssociation(){
        Dao<ImmutableActor> actorDao = ImmutableMediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        ImmutableActor hollyHunter = actorDao.select(where("name", Operator.EQUALS, "Holly Hunter")).get(0);

        Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        ImmutableMovie raisingArizona = movieDao.select(where("title", Operator.EQUALS, "Raising Arizona")).get(0);

        {
            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            associationDao.deleteAssociation(hollyHunter, raisingArizona);
        }
        {
            AssociationDao<ImmutableActor, ImmutableMovie> associationDao = ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            List<ImmutableMovie> movies = associationDao.selectRightAssociates(hollyHunter);
            List<String> titles = movies.stream().map(m -> m.getTitle()).collect(Collectors.toList());
            AssertHelp.sameContents(new String[]{"Broadcast News"}, titles);
        }
    }

    @Test
    public void testAssociationDaoSelect_LeftAssociates(){
        Dao<ImmutableMovie> movieDao = ImmutableMediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        ImmutableMovie raisingArizona = movieDao.select(where("title", Operator.EQUALS, "Raising Arizona")).get(0);

        AssociationDao<ImmutableActor, ImmutableMovie> associationDao =  ImmutableMediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
        List<ImmutableActor> actors = associationDao.selectLeftAssociates(raisingArizona);

        List<String> names = actors.stream().map(a -> a.getName()).collect(Collectors.toList());
        AssertHelp.sameContents(new String[]{"Holly Hunter", "Nicolas Cage"}, names);
    }

}
