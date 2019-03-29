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

    private static Helper helper = HelperFactory.forSchema("foo");

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
    }

    private static IndirectDaoBuilder<Foo,Foo> daoBuilder(){
        GenericColumn<Integer> integerColumn = new GenericColumn<>(
                PreparedStatement::setInt,
                ResultSet::getInt,
                Types.INTEGER
        );

        return new IndirectDaoBuilder<>("foo_table", Foo::new, f -> f)
                .withPrimaryKey("id", "foo_seq", Foo::getId, Foo::setId)
                .withGenericColumn("data", Foo::getData, Foo::setData, integerColumn);
    }

    @Test
    public void insertAndSelectItem() {
        Long id = helper.useAndCommitConnection( connection -> {

            Dao<Foo> dao = daoBuilder().buildDao(connection);

            Foo foo = new Foo();
            foo.setData(32);

            return  dao.insert(foo);
        });
        helper.useConnection(connection -> {
            Dao<Foo> dao = daoBuilder().buildDao(connection);
            Foo foo = dao.select(id);
            Assert.assertEquals(32, (int) foo.getData());
        });
    }
}
