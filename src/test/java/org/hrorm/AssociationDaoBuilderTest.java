package org.hrorm;

import org.hrorm.examples.media.Actor;
import org.hrorm.examples.media.MediaDaoBuilders;
import org.hrorm.examples.media.Movie;
import org.junit.Assert;
import org.junit.Test;

public class AssociationDaoBuilderTest {

    @Test
    public void testReadyAfterConstruction(){
        AssociationDaoBuilder<Movie, Actor> daoBuilder =
                new AssociationDaoBuilder<>(MediaDaoBuilders.MOVIE_DAO_BUILDER, MediaDaoBuilders.ACTOR_DAO_BUILDER);

        Assert.assertFalse(daoBuilder.ready());
    }

    @Test
    public void testReadyAferTableName(){
        AssociationDaoBuilder<Movie, Actor> daoBuilder =
                new AssociationDaoBuilder<>(MediaDaoBuilders.MOVIE_DAO_BUILDER, MediaDaoBuilders.ACTOR_DAO_BUILDER)
                .withTableName("actor_movie_association");

        Assert.assertFalse(daoBuilder.ready());
    }

    @Test
    public void testReadyAfterSeveralSet(){
        AssociationDaoBuilder<Movie, Actor> daoBuilder =
                new AssociationDaoBuilder<>(MediaDaoBuilders.MOVIE_DAO_BUILDER, MediaDaoBuilders.ACTOR_DAO_BUILDER)
                        .withTableName("actor_movie_association")
                        .withLeftColumnName("actor_id")
                        .withPrimaryKeyName("id");

        Assert.assertFalse(daoBuilder.ready());
    }

    @Test
    public void testReadyAfterAllSet(){
        AssociationDaoBuilder<Movie, Actor> daoBuilder =
                new AssociationDaoBuilder<>(MediaDaoBuilders.MOVIE_DAO_BUILDER, MediaDaoBuilders.ACTOR_DAO_BUILDER)
                        .withTableName("actor_movie_association")
                        .withRightColumnName("movie_id")
                        .withLeftColumnName("actor_id")
                        .withPrimaryKeyName("id")
                        .withSequenceName("actor_movie_sequence");

        Assert.assertTrue(daoBuilder.ready());
    }

}
