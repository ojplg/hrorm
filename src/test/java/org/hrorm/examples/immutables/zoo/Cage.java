package org.hrorm.examples.immutables.zoo;

import lombok.Builder;
import lombok.Data;
import org.hrorm.util.ListUtil;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class Cage {

    private final Long id;
    private final BigDecimal area;
    private List<Parrot> parrots;

    public static Cage newCage(BigDecimal area, List<String> parrotNames){
        List<Parrot> parrots = ListUtil.map(parrotNames, Parrot::newParrot);
        return new Cage(null, area, parrots);
    }

    public List<String> getParrotNames(){
        return ListUtil.map(parrots, Parrot::getName);
    }


}
