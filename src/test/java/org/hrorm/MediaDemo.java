package org.hrorm;

import lombok.Data;
import org.hrorm.examples.Actor;
import org.hrorm.examples.Movie;
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

public class MediaDemo {

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

    private static final DaoBuilder<Movie> MOVIE_DAO_BUILDER =
            new DaoBuilder<>("movies", Movie::new)
                .withPrimaryKey("id", "movies_sequence", Movie::getId, Movie::setId)
                .withStringColumn("title", Movie::getTitle, Movie::setTitle);


    private static final DaoBuilder<Actor> ACTOR_DAO_BUILDER =
            new DaoBuilder<>("actors", Actor::new)
                .withPrimaryKey("id", "actors_sequence", Actor::getId, Actor::setId)
                .withStringColumn("name", Actor::getName, Actor::setName);

    class Association<U, V> {
        U u;
        V v;
    }

    @Data
    static class ActorMovieAssociation {
        private Long id;
        private Actor actor;
        private Movie movie;
    }

    private static final DaoBuilder<ActorMovieAssociation> ACTOR_MOVIE_ASSOCIATION_DAO_BUILDER =
            new DaoBuilder<>("actor_movie_associations", ActorMovieAssociation::new)
                    .withPrimaryKey("id", "actor_movie_association_sequence", ActorMovieAssociation::getId, ActorMovieAssociation::setId)
                    .withJoinColumn("actor_id", ActorMovieAssociation::getActor, ActorMovieAssociation::setActor, ACTOR_DAO_BUILDER)
                    .withJoinColumn("movie_id", ActorMovieAssociation::getMovie, ActorMovieAssociation::setMovie, MOVIE_DAO_BUILDER);

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
        Dao<Movie> movieDao = MOVIE_DAO_BUILDER.buildDao(helper.connect());
        for(String title : MOVIE_TITLES){
            Movie movie = new Movie();
            movie.setTitle(title);
            movieDao.insert(movie);
        }
    }

    private void insertActors(){
        Dao<Actor> actorDao = ACTOR_DAO_BUILDER.buildDao(helper.connect());
        for(String name : ACTOR_NAMES){
            Actor actor = new Actor();
            actor.setName(name);
            actorDao.insert(actor);
        }
    }

    private void insertAssociations(){
        Connection connection = helper.connect();

        Dao<Movie> movieDao = MOVIE_DAO_BUILDER.buildDao(connection);
        Dao<Actor> actorDao = ACTOR_DAO_BUILDER.buildDao(connection);
        Dao<ActorMovieAssociation> actorMovieAssociationDao = ACTOR_MOVIE_ASSOCIATION_DAO_BUILDER.buildDao(connection);

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

        Dao<Actor> actorDao = ACTOR_DAO_BUILDER.buildDao(connection);
        Dao<ActorMovieAssociation> actorMovieAssociationDao = ACTOR_MOVIE_ASSOCIATION_DAO_BUILDER.buildDao(connection);

        Actor graceKelly = actorDao.select(where("name", Operator.EQUALS, "Grace Kelly")).get(0);

        List<ActorMovieAssociation> associations = actorMovieAssociationDao.select(
                where("actor_id", Operator.EQUALS, graceKelly.getId()));

        List<String> movieTitles = associations.stream().map(assoc -> assoc.getMovie().getTitle()).collect(Collectors.toList());

        AssertHelp.sameContents(new String[]{"High Noon", "To Catch A Thief"}, movieTitles);
    }
}
