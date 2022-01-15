package org.hrorm.examples.immutables.zoo;

import lombok.Builder;
import lombok.Data;
import org.hrorm.util.ListUtil;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class Room {

    private final Long id;
    private final String name;
    private final Cage cage;

    public BigDecimal getCageArea(){
        return cage.getArea();
    }

    public List<Parrot> getParrots(){
        return cage.getParrots();
    }

    public List<String> getParrotNames(){
        return cage.getParrotNames();
    }

    public static Room newRoom(String name, Cage cage){
        return new Room(null, name, cage);
    }
}
