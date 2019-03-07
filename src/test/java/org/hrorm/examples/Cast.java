package org.hrorm.examples;

import lombok.Data;

import java.util.List;

@Data
public class Cast {
    private Movie movie;
    private List<Actor> actors;
}
