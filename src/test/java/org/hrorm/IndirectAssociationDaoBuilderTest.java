package org.hrorm;

import org.hrorm.examples.Actor;
import org.hrorm.examples.Movie;
import org.junit.Assert;
import org.junit.Test;

public class IndirectAssociationDaoBuilderTest {

    @Test
    public void testReadyAfterConstruction(){
        IndirectAssociationDaoBuilder<Movie, Movie, Actor, Actor> daoBuilder =
                new IndirectAssociationDaoBuilder<>(MediaDemo.MOVIE_DAO_BUILDER, MediaDemo.ACTOR_DAO_BUILDER);

        Assert.assertFalse(daoBuilder.ready());
    }

    @Test
    public void testReadyAferTableName(){
        IndirectAssociationDaoBuilder<Movie, Movie, Actor, Actor> daoBuilder =
                new IndirectAssociationDaoBuilder<>(MediaDemo.MOVIE_DAO_BUILDER, MediaDemo.ACTOR_DAO_BUILDER)
                .withTableName("actor_movie_association");

        Assert.assertFalse(daoBuilder.ready());
    }

    @Test
    public void testReadyAfterSeveralSet(){
        IndirectAssociationDaoBuilder<Movie, Movie, Actor, Actor> daoBuilder =
                new IndirectAssociationDaoBuilder<>(MediaDemo.MOVIE_DAO_BUILDER, MediaDemo.ACTOR_DAO_BUILDER)
                        .withTableName("actor_movie_association")
                        .withLeftColumnName("actor_id")
                        .withPrimaryKeyName("id");

        Assert.assertFalse(daoBuilder.ready());
    }

    @Test
    public void testReadyAfterAllSet(){
        IndirectAssociationDaoBuilder<Movie, Movie, Actor, Actor> daoBuilder =
                new IndirectAssociationDaoBuilder<>(MediaDemo.MOVIE_DAO_BUILDER, MediaDemo.ACTOR_DAO_BUILDER)
                        .withTableName("actor_movie_association")
                        .withRightColumnName("movie_id")
                        .withLeftColumnName("actor_id")
                        .withPrimaryKeyName("id")
                        .withSequenceName("actor_movie_sequence");

        Assert.assertTrue(daoBuilder.ready());
    }

}
