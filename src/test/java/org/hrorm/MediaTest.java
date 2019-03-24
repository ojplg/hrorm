package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.examples.media.Actor;
import org.hrorm.examples.media.ActorMovieAssociation;
import org.hrorm.examples.media.MediaDaoBuilders;
import org.hrorm.examples.media.Movie;
import org.hrorm.database.H2Helper;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hrorm.Where.where;

public class MediaTest {

    static { TestLogConfig.load(); }

    private static Helper helper = new H2Helper("media");

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
        Dao<Movie> movieDao = MediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        for(String title : MOVIE_TITLES){
            Movie movie = new Movie();
            movie.setTitle(title);
            movieDao.insert(movie);
        }
    }

    private void insertActors(){
        Dao<Actor> actorDao = MediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        for(String name : ACTOR_NAMES){
            Actor actor = new Actor();
            actor.setName(name);
            actorDao.insert(actor);
        }
    }

    private void insertAssociations(){
        Connection connection = helper.connect();

        Dao<Movie> movieDao = MediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(connection);
        Dao<Actor> actorDao = MediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(connection);
        Dao<ActorMovieAssociation> actorMovieAssociationDao = MediaDaoBuilders.ACTOR_MOVIE_ASSOCIATION_DAO_BUILDER.buildDao(connection);

        List<Actor> actors = actorDao.selectAll();
        List<Movie> movies = movieDao.selectAll();

        for( String[] pair : ASSOCIATIONS ){
            String movieName = pair[0];
            String actorName = pair[1];

            Actor actor = findActor(actors, actorName);
            Movie movie = findMovie(movies, movieName);

            ActorMovieAssociation association = new ActorMovieAssociation();
            association.setActor(actor);
            association.setMovie(movie);

            actorMovieAssociationDao.insert(association);
        }
    }

    private Actor findActor(List<Actor> actors, String name){
        return actors.stream().filter(a -> a.getName().equals(name)).findFirst().get();
    }

    private Movie findMovie(List<Movie> movies, String title){
        return movies.stream().filter(m -> m.getTitle().equals(title)).findFirst().get();
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
    public void testFindGraceKellyMovies(){
        Connection connection = helper.connect();

        Dao<Actor> actorDao = MediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(connection);
        Dao<ActorMovieAssociation> actorMovieAssociationDao = MediaDaoBuilders.ACTOR_MOVIE_ASSOCIATION_DAO_BUILDER.buildDao(connection);

        Actor graceKelly = actorDao.select(where("name", Operator.EQUALS, "Grace Kelly")).get(0);

        List<ActorMovieAssociation> associations = actorMovieAssociationDao.select(
                where("actor_id", Operator.EQUALS, graceKelly.getId()));

        List<String> movieTitles = associations.stream().map(assoc -> assoc.getMovie().getTitle()).collect(Collectors.toList());

        AssertHelp.sameContents(new String[]{"High Noon", "To Catch A Thief"}, movieTitles);
    }


    @Test
    public void testAssociationDaoSelect(){
        Dao<Actor> actorDao = MediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        Actor graceKelly = actorDao.select(where("name", Operator.EQUALS, "Grace Kelly")).get(0);

        AssociationDao<Actor, Movie> associationDao = MediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
        List<Movie> movies = associationDao.selectRightAssociates(graceKelly);

        List<String> titles = movies.stream().map(Movie::getTitle).collect(Collectors.toList());
        AssertHelp.sameContents(new String[]{"High Noon", "To Catch A Thief"}, titles);
    }

    @Test
    public void testInsertAssociation(){
        Dao<Actor> actorDao = MediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        Actor caryGrant = actorDao.select(where("name", Operator.EQUALS, "Cary Grant")).get(0);

        Dao<Movie> movieDao = MediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        Movie philadelphiaStory = new Movie();
        philadelphiaStory.setTitle("The Philadelphia Story");
        movieDao.insert(philadelphiaStory);

        {
            AssociationDao<Actor, Movie> associationDao = MediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            Long id = associationDao.insertAssociation(caryGrant, philadelphiaStory);
            Assert.assertNotNull(id);
        }
        {
            AssociationDao<Actor, Movie> associationDao = MediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            List<Movie> movies = associationDao.selectRightAssociates(caryGrant);
            List<String> titles = movies.stream().map(Movie::getTitle).collect(Collectors.toList());
            AssertHelp.sameContents(new String[]{"North By Northwest", "To Catch A Thief", "The Philadelphia Story"}, titles);
        }
    }

    @Test
    public void testDeleteAssociation(){
        Dao<Actor> actorDao = MediaDaoBuilders.ACTOR_DAO_BUILDER.buildDao(helper.connect());
        Actor hollyHunter = actorDao.select(where("name", Operator.EQUALS, "Holly Hunter")).get(0);

        Dao<Movie> movieDao = MediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        Movie raisingArizona = movieDao.select(where("title", Operator.EQUALS, "Raising Arizona")).get(0);

        {
            AssociationDao<Actor, Movie> associationDao = MediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            associationDao.deleteAssociation(hollyHunter, raisingArizona);
        }
        {
            AssociationDao<Actor, Movie> associationDao = MediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
            List<Movie> movies = associationDao.selectRightAssociates(hollyHunter);
            List<String> titles = movies.stream().map(Movie::getTitle).collect(Collectors.toList());
            AssertHelp.sameContents(new String[]{"Broadcast News"}, titles);
        }
    }

    @Test
    public void testAssociationDaoSelect_LeftAssociates(){
        Dao<Movie> movieDao = MediaDaoBuilders.MOVIE_DAO_BUILDER.buildDao(helper.connect());
        Movie raisingArizona = movieDao.select(where("title", Operator.EQUALS, "Raising Arizona")).get(0);

        AssociationDao<Actor, Movie> associationDao = MediaDaoBuilders.ASSOCIATION_DAO_BUILDER.buildDao(helper.connect());
        List<Actor> actors = associationDao.selectLeftAssociates(raisingArizona);

        List<String> names = actors.stream().map(Actor::getName).collect(Collectors.toList());
        AssertHelp.sameContents(new String[]{"Holly Hunter", "Nicolas Cage"}, names);
    }

}
