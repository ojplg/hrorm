package org.hrorm.examples;

import lombok.Data;
import org.hrorm.DaoBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
class Person {
    Long id;
    String name;
    long weight;
    BigDecimal height;
    LocalDateTime birthday;
    boolean isHighSchoolGraduate;
    HairColor hairColor;

    private void builderDemo() {
        DaoBuilder<Person> daoBuilder = new DaoBuilder<>("PERSON_TABLE", Person::new);
        daoBuilder.withPrimaryKey("ID","PERSON_SEQUENCE", Person::getId, Person::setId);
    }

}



