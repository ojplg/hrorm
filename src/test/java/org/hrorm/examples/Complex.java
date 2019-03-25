package org.hrorm.examples;

import lombok.Data;
import org.hrorm.DaoBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Complex {

    private static final Random random = new Random();

    private static String randomSmallWord(){
        int length = random.nextInt(8);
        StringBuffer buf = new StringBuffer();
        for(int idx=0; idx<length; idx++ ){
            int i = random.nextInt(26) + 65;
            char c = (char) i;
            buf.append(c);
        }
        return buf.toString();
    }

    private static BigDecimal randomBigDecimal() {
        int base = random.nextInt(10000000);
        int divideBy = 10 + random.nextInt(1000);
        float val = (float) base / (float) divideBy;
        return new BigDecimal(val);
    }

    @Data
    public static class Ann {
        Long id;
        String name;
        List<Cal> cals;
        Beth beth;

        @Override
        public String toString() {
            return "Ann{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", cals=" + cals +
                    ", beth=" + beth +
                    '}';
        }
    }

    public static Ann newAnn(Beth beth, Cal ... cals){
        Ann ann = new Ann();
        ann.setName(randomSmallWord());
        ann.setBeth(beth);
        ann.setCals(Arrays.asList(cals));
        return ann;
    }

    @Data
    public static class Beth {
        Long id;
        Long number;
        List<Don> dons;
        List<Edith> ediths;

        @Override
        public String toString() {
            return "Beth{" +
                    "id=" + id +
                    ", number=" + number +
                    ", dons=" + dons +
                    ", ediths=" + ediths +
                    '}';
        }
    }

    public static Beth newBeth(List<Don> dons, List<Edith> ediths){
        Beth beth = new Beth();
        beth.setNumber((long) random.nextInt(10000000));
        beth.setDons(dons);
        beth.setEdiths(ediths);
        return beth;
    }

    @Data
    public static class Cal {
        Long id;
        BigDecimal amount;
        Ann ann;

        @Override
        public String toString() {
            return "Cal{" +
                    "id=" + id +
                    ", amount=" + amount +
                    '}';
        }
    }

    public static Cal newCal(){
        Cal cal = new Cal();
        cal.setAmount(randomBigDecimal());
        return cal;
    }

    @Data
    public static class Don {
        Long id;
        Instant dateTime;
        Long quantity;
        Beth beth;
        List<Henry> henries;

        @Override
        public String toString() {
            return "Don{" +
                    "id=" + id +
                    ", dateTime=" + dateTime +
                    ", quantity=" + quantity +
                    ", henries=" + henries +
                    '}';
        }
    }

    public static Don newDon(Henry ... henries){
        Don don = new Don();
        don.setDateTime(Instant.now());
        don.setQuantity((long) random.nextInt(1000000));
        don.setHenries(Arrays.asList(henries));
        return don;
    }

    @Data
    public static class Edith {
        Long id;
        String word;
        Long length;
        Beth beth;
        Fred fred;
        Gap gap;

        @Override
        public String toString() {
            return "Edith{" +
                    "id=" + id +
                    ", word='" + word + '\'' +
                    ", length=" + length +
                    ", fred=" + fred +
                    ", gap=" + gap +
                    '}';
        }
    }

    public static Edith newEdith(Fred fred, Gap gap){
        Edith edith = new Edith();
        edith.setWord(randomSmallWord());
        edith.setLength((long)random.nextInt(10000));
        edith.setFred(fred);
        edith.setGap(gap);
        return edith;
    }

    @Data
    public static class Fred {
        Long id;
        Boolean flag;
    }

    public static Fred newFred(){
        Fred fred = new Fred();
        fred.setFlag(random.nextBoolean());
        return fred;
    }

    @Data
    public static class Gap {
        Long id;
        String insignia;
    }

    public static Gap newGap(){
        Gap gap = new Gap();
        gap.setInsignia(randomSmallWord());
        return gap;
    }

    @Data
    public static class Henry {
        Long id;
        BigDecimal fraction;
        Long amount;
        Don don;
        Ida ida;

        @Override
        public String toString() {
            return "Henry{" +
                    "id=" + id +
                    ", fraction=" + fraction +
                    ", amount=" + amount +
                    ", ida=" + ida +
                    '}';
        }
    }

    public static Henry newHenry(Ida ida){
        Henry henry = new Henry();
        henry.setFraction(randomBigDecimal());
        henry.setAmount((long)random.nextInt(100000));
        henry.setIda(ida);
        return henry;
    }

    @Data
    public static class Ida {
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

    public static Ida newIda(Jules jules){
        Ida ida = new Ida();
        ida.setName(randomSmallWord());
        ida.setJules(jules);
        return ida;
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
            .withParentColumn("don_id", Henry::getDon, Henry::setDon)
            .withJoinColumn("ida_id", Henry::getIda, Henry::setIda, idaDaoBuilder);

    public static DaoBuilder<Gap> gapDaoBuilder = new DaoBuilder<>("gap", Gap::new)
            .withPrimaryKey("id", "gap_sequence", Gap::getId, Gap::setId)
            .withStringColumn("insignia", Gap::getInsignia, Gap::setInsignia);

    public static DaoBuilder<Fred> fredDaoBuilder = new DaoBuilder<>("fred", Fred::new)
            .withPrimaryKey("id", "fred_sequence", Fred::getId, Fred::setId)
            .withBooleanColumn("flag", Fred::getFlag, Fred::setFlag);

    public static DaoBuilder<Edith> edithDaoBuilder = new DaoBuilder<>("edith", Edith::new)
            .withPrimaryKey("id", "edith_sequence", Edith::getId, Edith::setId)
            .withStringColumn("word", Edith::getWord, Edith::setWord)
            .withIntegerColumn("length", Edith::getLength, Edith::setLength)
            .withJoinColumn("fred_id", Edith::getFred, Edith::setFred, fredDaoBuilder)
            .withJoinColumn("gap_id", Edith::getGap, Edith::setGap, gapDaoBuilder)
            .withParentColumn("beth_id", Edith::getBeth, Edith::setBeth);

    public static DaoBuilder<Don> donDaoBuilder = new DaoBuilder<>("don", Don::new)
            .withPrimaryKey("id", "don_sequence", Don::getId, Don::setId)
            .withInstantColumn("datetime", Don::getDateTime, Don::setDateTime)
            .withIntegerColumn("quantity", Don::getQuantity, Don::setQuantity)
            .withChildren(Don::getHenries, Don::setHenries, henryDaoBuilder)
            .withParentColumn("beth_id", Don::getBeth, Don::setBeth);

    public static DaoBuilder<Cal> calDaoBuilder = new DaoBuilder<>("cal", Cal::new)
            .withPrimaryKey("id", "cal_sequence", Cal::getId, Cal::setId)
            .withBigDecimalColumn("amount", Cal::getAmount, Cal::setAmount)
            .withParentColumn("ann_id", Cal::getAnn, Cal::setAnn);

    public static DaoBuilder<Beth> bethDaoBuilder = new DaoBuilder<>("beth", Beth::new)
            .withPrimaryKey("id", "beth_sequence", Beth::getId, Beth::setId)
            .withIntegerColumn("number", Beth::getNumber, Beth::setNumber)
            .withChildren(Beth::getDons, Beth::setDons, donDaoBuilder)
            .withChildren(Beth::getEdiths, Beth::setEdiths, edithDaoBuilder);

    public static DaoBuilder<Ann> annDaoBuilder = new DaoBuilder<>("ann", Ann::new)
            .withPrimaryKey("id", "ann_sequence", Ann::getId, Ann::setId)
            .withStringColumn("name", Ann::getName, Ann::setName)
            .withChildren(Ann::getCals, Ann::setCals, calDaoBuilder)
            .withJoinColumn("beth_id", Ann::getBeth, Ann::setBeth, bethDaoBuilder);
}
