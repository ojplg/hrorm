package org.hrorm;

import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Test;

public class SchemaTest {

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

}
