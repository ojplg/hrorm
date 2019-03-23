package org.hrorm.examples.media;

import org.hrorm.AssociationDaoBuilder;
import org.hrorm.IndirectDaoBuilder;

public class ImmutableMediaDaoBuilders {

    public static final IndirectDaoBuilder<ImmutableMovie, ImmutableMovie.ImmutableMovieBuilder> MOVIE_DAO_BUILDER =
            new IndirectDaoBuilder<>("movies", ImmutableMovie.ImmutableMovieBuilder::new, ImmutableMovie.ImmutableMovieBuilder::build)
                    .withPrimaryKey("id", "movies_sequence", ImmutableMovie::getId, ImmutableMovie.ImmutableMovieBuilder::id)
                    .withStringColumn("title", ImmutableMovie::getTitle, ImmutableMovie.ImmutableMovieBuilder::title);


    public static final IndirectDaoBuilder<ImmutableActor, ImmutableActor.ImmutableActorBuilder> ACTOR_DAO_BUILDER =
            new IndirectDaoBuilder<>("actors", ImmutableActor.ImmutableActorBuilder::new, ImmutableActor.ImmutableActorBuilder::build)
                    .withPrimaryKey("id", "actors_sequence", ImmutableActor::getId, ImmutableActor.ImmutableActorBuilder::id)
                    .withStringColumn("name", ImmutableActor::getName, ImmutableActor.ImmutableActorBuilder::name);


    public static final AssociationDaoBuilder<ImmutableActor, ImmutableMovie> ASSOCIATION_DAO_BUILDER =
            new AssociationDaoBuilder<>(ACTOR_DAO_BUILDER, MOVIE_DAO_BUILDER)
                    .withTableName("actor_movie_associations")
                    .withSequenceName("actor_movie_association_sequence")
                    .withPrimaryKeyName("id")
                    .withLeftColumnName("actor_id")
                    .withRightColumnName("movie_id");

}