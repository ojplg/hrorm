package org.hrorm;

import lombok.Data;
import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class GenericColumnTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("generic_columns_test");

    @BeforeClass
    public static void setUpDb(){
        Schema schema = new Schema(daoBuilder());
        String sql = schema.sql();
        helper.initializeSchemaFromSql(sql);
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }


    @Data
    static class Foo {
        private Long id;
        private Integer data;
        private String junk;
    }

    private static IndirectDaoBuilder<Foo,Foo> daoBuilder(){
        GenericColumn<Integer> integerColumn = new GenericColumn<>(
                PreparedStatement::setInt,
                ResultSet::getInt,
                Types.INTEGER,
                "integer"
        );

        Converter<String, Integer> converter = new Converter<String, Integer>() {
            @Override
            public Integer from(String item) {
                return Integer.parseInt(item);
            }

            @Override
            public String to(Integer integer) {
                if( integer == null ){
                    return null;
                }
                return integer.toString();
            }
        };

        return new IndirectDaoBuilder<>("foo_table", Foo::new, f -> f)
                .withPrimaryKey("id", "foo_seq", Foo::getId, Foo::setId)
                .withGenericColumn("data", Foo::getData, Foo::setData, integerColumn)
                .withConvertedGenericColumn("junk", Foo::getJunk, Foo::setJunk, integerColumn, converter);
    }

    @Test
    public void insertAndSelectItem() {
        Long id = helper.useConnection( connection -> {

            Dao<Foo> dao = daoBuilder().buildDao(connection);

            Foo foo = new Foo();
            foo.setData(32);
            foo.setJunk("6732");

            return  dao.insert(foo);
        });
        helper.useConnection(connection -> {
            Dao<Foo> dao = daoBuilder().buildDao(connection);
            Foo foo = dao.select(id);
            Assert.assertEquals(32, (int) foo.getData());
            Assert.assertEquals("6732", foo.getJunk());
        });
    }

    @Test
    public void testNulls() {
        Long id = helper.useConnection( connection -> {

            Dao<Foo> dao = daoBuilder().buildDao(connection);

            Foo foo = new Foo();

            return  dao.insert(foo);
        });
        helper.useConnection(connection -> {
            Dao<Foo> dao = daoBuilder().buildDao(connection);
            Foo foo = dao.select(id);
            Assert.assertNull(foo.getData());
            Assert.assertNull(foo.getJunk());
        });
    }

}
