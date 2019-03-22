package org.hrorm;

import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.examples.Simple;
import org.hrorm.examples.geography.GeographyDaos;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SchemaTest {

    // TODO: Generate schema and test by instantiating inside H2
    // TODO: Association tables
    // TODO: Unique constraints? (Hrorm knows nothing about these.)
    // TODO: keyless daos?

    @Test
    public void testCreateTableSql(){

        Schema columnsSchema = new Schema(ColumnsDaoBuilder.DAO_BUILDER);

        String createTableSql = columnsSchema.createTableSql("colUMNs_table");

        String expectedSql = "create table columns_table (\n" +
                "  id integer PRIMARY KEY,\n" +
                "  string_column text,\n" +
                "  integer_column integer,\n" +
                "  decimal_column decimal,\n" +
                "  boolean_column boolean,\n" +
                "  timestamp_column timestamp,\n" +
                "  color_column text\n" +
                ");";

        SimpleSqlFormatter.assertEqualSql(expectedSql, createTableSql);

    }

    @Test
    public void testCreateSequence(){

        Schema columnsSchema = new Schema(ColumnsDaoBuilder.DAO_BUILDER);

        String createSequenceSql = columnsSchema.createSequenceSql("colUMns_Seq");

        String expectedSql = "create sequence columns_seq;\n";

        SimpleSqlFormatter.assertEqualSql(expectedSql, createSequenceSql);
    }

    @Test
    public void testSql(){
        Schema columnsSchema = new Schema(ColumnsDaoBuilder.DAO_BUILDER);
        String sql = columnsSchema.sql();

        H2Helper helper = new H2Helper("columns");
        String expectedSql = helper.readSchema();

        SimpleSqlFormatter.assertEqualSql(expectedSql, sql);
    }

    @Test
    public void testNotNullColumn(){
        DaoBuilder<Simple> daoBuilder = new DaoBuilder<>("siMplE", Simple::new)
                .withPrimaryKey("id", "simple_seq", Simple::getId, Simple::setId)
                .withStringColumn("field", Simple::getField, Simple::setField).notNull();

        Schema schema = new Schema(daoBuilder);
        String sql = schema.createTableSql("Simple");

        String expectedSql = "create table simple (\n" +
                " id integer primary key,\n" +
                " field text not null\n" +
                ");";

        SimpleSqlFormatter.assertEqualSql(expectedSql, sql);
    }

    @Test
    public void testForeignKeyConstraint(){
        Schema schema = new Schema(GeographyDaos.CityDaoBuilder, GeographyDaos.StateDaoBuilder);

        List<String> constraints = schema.constraints();
        Assert.assertEquals(1, constraints.size());

        String expectedSql = "alter table city add foreign key (state_id) references state(id);";

        SimpleSqlFormatter.assertEqualSql(expectedSql, constraints.get(0));
    }

}
