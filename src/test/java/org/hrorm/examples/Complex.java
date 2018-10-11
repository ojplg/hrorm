package org.hrorm.examples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import lombok.Data;
import org.hrorm.DaoBuilder;

public class Complex {

    private static final Random random = new Random();

    @Data
    public static class Ann {
        Long id;
        String name;
        List<Cal> cals;
        Beth beth;
    }

    @Data
    static class Beth {
        Long id;
        Long number;
        List<Don> dons;
        List<Edith> ediths;
    }

    @Data
    static class Cal {
        Long id;
        BigDecimal amount;
        Ann ann;
    }

    @Data
    static class Don {
        Long id;
        LocalDateTime dateTime;
        Long quantity;
        Beth beth;
        List<Henry> henries;
    }

    @Data
    static class Edith {
        Long id;
        String word;
        Long length;
        Beth beth;
        Fred fred;
        Gap gap;
    }

    @Data
    static class Fred {
        Long id;
        Boolean flag;
    }

    @Data
    static class Gap {
        Long id;
        String insignia;
    }

    @Data
    static class Henry {
        Long id;
        BigDecimal fraction;
        Long amount;
        Don don;
        Ida ida;
    }

    @Data
    static class Ida {
        Long id;
        String name;
        Jules jules;
    }

    @Data
    public static class Jules {
        Long id;
        Long magnitude;
    }

    public static Jules newJules(){
        Jules jules = new Jules();
        jules.setMagnitude((long)random.nextInt(100000));
        return jules;
    }

    public static DaoBuilder<Jules> julesDaoBuilder = new DaoBuilder<>("jules", Jules::new)
            .withPrimaryKey("id", "jules_sequence", Jules::getId, Jules::setId)
            .withIntegerColumn("magnitude", Jules::getMagnitude, Jules::setMagnitude);

    public static DaoBuilder<Ida> idaDaoBuilder = new DaoBuilder<>("ida", Ida::new)
            .withPrimaryKey("id", "ida_sequence", Ida::getId, Ida::setId)
            .withStringColumn("name", Ida::getName, Ida::setName)
            .withJoinColumn("jules_id", Ida::getJules, Ida::setJules, julesDaoBuilder);

    public static DaoBuilder<Henry> henryDaoBuilder = new DaoBuilder<>("henry", Henry::new)
            .withPrimaryKey("id", "henry_sequence", Henry::getId, Henry::setId)
            .withBigDecimalColumn("fraction", Henry::getFraction, Henry::setFraction)
            .withIntegerColumn("amount", Henry::getAmount, Henry::setAmount)
            .withParentColumn("don_id", Henry::getDon, Henry::setDon);

    public static DaoBuilder<Gap> gapDaoBuilder = new DaoBuilder<>("gap", Gap::new)
            .withPrimaryKey("id", "gap_sequence", Gap::getId, Gap::setId)
            .withStringColumn("insignia", Gap::getInsignia, Gap::setInsignia);

    public static DaoBuilder<Fred> fredDaoBuilder = new DaoBuilder<>("fred", Fred::new)
            .withPrimaryKey("id", "fred_sequence", Fred::getId, Fred::setId)
            .withBooleanColumn("flag", Fred::getFlag, Fred::setFlag);

    public static DaoBuilder<Edith> edithDaoBuilder = new DaoBuilder<>("edith", Edith::new)
            .withPrimaryKey("id", "fred_sequence", Edith::getId, Edith::setId)
            .withStringColumn("word", Edith::getWord, Edith::setWord)
            .withIntegerColumn("length", Edith::getLength, Edith::setLength)
            .withJoinColumn("fred_id", Edith::getFred, Edith::setFred, fredDaoBuilder)
            .withJoinColumn("gap_id", Edith::getGap, Edith::setGap, gapDaoBuilder)
            .withParentColumn("beth_id", Edith::getBeth, Edith::setBeth);

    public static DaoBuilder<Don> donDaoBuilder = new DaoBuilder<>("don", Don::new)
            .withPrimaryKey("id", "don_sequence", Don::getId, Don::setId)
            .withLocalDateTimeColumn("datetime", Don::getDateTime, Don::setDateTime)
            .withIntegerColumn("quantity", Don::getQuantity, Don::setQuantity)
            .withChildren(Don::getHenries, Don::setHenries, henryDaoBuilder)
            .withParentColumn("beth_id", Don::getBeth, Don::setBeth);

    public static DaoBuilder<Cal> calDaoBuilder = new DaoBuilder<>("cal", Cal::new)
            .withPrimaryKey("id", "cal_id", Cal::getId, Cal::setId)
            .withBigDecimalColumn("amount", Cal::getAmount, Cal::setAmount)
            .withParentColumn("ann_id", Cal::getAnn, Cal::setAnn);

    public static DaoBuilder<Beth> bethDaoBuilder = new DaoBuilder<>("beth", Beth::new)
            .withPrimaryKey("id", "beth_id", Beth::getId, Beth::setId)
            .withIntegerColumn("number", Beth::getNumber, Beth::setNumber)
            .withChildren(Beth::getDons, Beth::setDons, donDaoBuilder)
            .withChildren(Beth::getEdiths, Beth::setEdiths, edithDaoBuilder);

    public static DaoBuilder<Ann> annDaoBuilder = new DaoBuilder<>("ann", Ann::new)
            .withPrimaryKey("id", "ann_sequence", Ann::getId, Ann::setId)
            .withStringColumn("name", Ann::getName, Ann::setName)
            .withChildren(Ann::getCals, Ann::setCals, calDaoBuilder);
}
