package org.hrorm.examples;

import org.hrorm.DaoBuilder;

public class GeographyDaos {

    DaoBuilder<State> stateDaoBuilder = new DaoBuilder<>("STATE", State::new)
            .withPrimaryKey("ID", "STATE_SEQUENCE", State::getId, State::setId)
            .withStringColumn("NAME", State::getName, State::setName);


    DaoBuilder<City> cityDaoBuilder = new DaoBuilder<>("CITY", City::new)
            .withPrimaryKey("ID", "CITY_SEQUENCE", City::getId, City::setId)
            .withStringColumn("NAME", City::getName, City::setName)
            .withJoinColumn("STATE_ID", City::getState, City::setState, stateDaoBuilder);
}
