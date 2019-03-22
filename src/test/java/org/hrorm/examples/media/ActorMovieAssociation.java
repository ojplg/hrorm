package org.hrorm.examples.media;

import lombok.Data;

@Data
public class ActorMovieAssociation {
    private Long id;
    private Actor actor;
    private Movie movie;
}
