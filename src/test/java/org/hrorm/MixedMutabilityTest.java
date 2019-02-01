package org.hrorm;

import lombok.Builder;
import lombok.Data;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

public class MixedMutabilityTest {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("mixed");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void testValidations(){
        Connection connection = helper.connect();
        Validator.validate(connection, forkBuilder);
        Validator.validate(connection, stuckBuilder);
        Validator.validate(connection, moverBuilder);
    }

    @Test
    public void testInsertAndSelect(){
        long id;
        {
            Connection connection = helper.connect();

            Fork fork = Fork.builder()
                    .measure(new BigDecimal("234.234"))
                    .build();

            Dao<Fork> forkDao = forkBuilder.buildDao(connection);

            long forkId = forkDao.insert(fork);

            fork = forkDao.select(forkId);

            Stuck stuck = Stuck.builder()
                    .name("FooBar")
                    .build();

            Mover mover = new Mover();
            mover.setFork(fork);
            mover.setStucks(Collections.singletonList(stuck));

            Dao<Mover> moverDao = moverBuilder.buildDao(connection);

            id = moverDao.insert(mover);
        }
        {
            Connection connection = helper.connect();
            Dao<Mover> moverDao = moverBuilder.buildDao(connection);

            Mover mover = moverDao.select(id);

            Assert.assertNotNull(mover);

            Assert.assertEquals(new BigDecimal("234.234"), mover.getFork().getMeasure());
            Assert.assertEquals("FooBar", mover.getStucks().get(0).getName());
        }

    }


    static final IndirectDaoBuilder<Stuck, Stuck.StuckBuilder> stuckBuilder =
            new IndirectDaoBuilder<>("STUCK", Stuck.StuckBuilder::new, Stuck.StuckBuilder::build)
                    .withPrimaryKey("ID", "MIXED_SEQUENCE", Stuck::getId, Stuck.StuckBuilder::id)
                    .withStringColumn("NAME", Stuck::getName, Stuck.StuckBuilder::name)
                    .withParentColumn("MOVER_ID");


    static final IndirectDaoBuilder<Fork, Fork.ForkBuilder> forkBuilder =
            new IndirectDaoBuilder<>("FORK", Fork.ForkBuilder::new, Fork.ForkBuilder::build)
                    .withPrimaryKey("ID", "MIXED_SEQUENCE", Fork::getId, Fork.ForkBuilder::id)
                    .withBigDecimalColumn("MEASURE", Fork::getMeasure, Fork.ForkBuilder::measure);

    static final DaoBuilder<Mover> moverBuilder =
            new DaoBuilder<>("MOVER", Mover::new)
                    .withPrimaryKey("ID", "MIXED_SEQUENCE", Mover::getId, Mover::setId)
                    .withJoinColumn("FORK_ID", Mover::getFork, Mover::setFork, forkBuilder)
                    .withChildren(Mover::getStucks, Mover::setStucks, stuckBuilder);

    @Data
    static class Mover {
        Long id;
        List<Stuck> stucks;
        Fork fork;
    }

    @Data
    @Builder
    static class Fork {
        final Long id;
        final BigDecimal measure;
    }

    @Data
    @Builder
    static class Stuck {
        final Long id;
        final String name;
    }
}

