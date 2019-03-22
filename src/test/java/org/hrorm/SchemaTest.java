package org.hrorm;

import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.examples.Simple;
import org.hrorm.examples.SimpleParentChildDaos;
import org.hrorm.examples.geography.GeographyDaos;
import org.hrorm.examples.immutables.DaoBuilders;
import org.hrorm.examples.media.MediaDaoBuilders;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SchemaTest {

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
    public void testForeignKeyConstraintJoins(){
        Schema schema = new Schema(GeographyDaos.CityDaoBuilder, GeographyDaos.StateDaoBuilder);

        List<String> constraints = schema.constraints();
        Assert.assertEquals(1, constraints.size());

        String expectedSql = "alter table city add foreign key (state_id) references state(id);";

        SimpleSqlFormatter.assertEqualSql(expectedSql, constraints.get(0));
    }

    @Test
    public void testForeignKeyConstraintParentChild(){
        Schema schema = new Schema(SimpleParentChildDaos.PARENT, SimpleParentChildDaos.CHILD);

        List<String> constraints = schema.constraints();
        Assert.assertEquals(1, constraints.size());

        String expectedSql = "alter table simple_child_table add foreign key (parent_id) references simple_parent_table(id);";
        SimpleSqlFormatter.assertEqualSql(expectedSql, constraints.get(0));
    }

    @Test
    public void testSchemaGenerationWorks(){
        Schema schema = new Schema(
                DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER,
                DaoBuilders.IMMUTABLE_SIBLING_DAO_BUILDER,
                DaoBuilders.IMMUTABLE_CHILD_DAO_BUILDER);

        String sql = schema.sql();

        // check to make sure there are two constraints, i.e. things having an "alter" in them
        String[] splits = sql.split("alter");
        Assert.assertEquals(3, splits.length);

        H2Helper helper = new H2Helper("generated_immutables");
        helper.initializeSchema(sql);

        ImmutableThingTest.doInsertAndSelectImmutableThing(helper);
        ImmutableThingTest.doInsertAndSelectImmutableThingWithAChild(helper);
        ImmutableThingTest.doInsertAndSelectImmutableThingWithAChildAndSibling(helper);
        ImmutableThingTest.doTestCascadingUpdate(helper);

        helper.dropSchema();
    }

    @Test
    public void testAssociationDaoSchemaTables(){
        Schema schema = new Schema(
                new DaoDescriptor[]{ },
                new AssociationDaoBuilder[]{ MediaDaoBuilders.ASSOCIATION_DAO_BUILDER });

        String sql = schema.createTableSql("actor_movie_associations");

        String expectedSql = "create table actor_movie_associations (" +
                " id integer primary key,\n" +
                "    actor_id integer not null,\n" +
                "    movie_id integer not null\n" +
                ");";

        SimpleSqlFormatter.assertEqualSql(expectedSql, sql);
    }

    @Test
    public void testAssociationDaoSchemaSequences(){
        Schema schema = new Schema(
                new DaoDescriptor[]{ },
                new AssociationDaoBuilder[]{ MediaDaoBuilders.ASSOCIATION_DAO_BUILDER });

        String sql = SimpleSqlFormatter.format(schema.sql());

        String expectedSql = SimpleSqlFormatter.format("create sequence actor_movie_association_sequence;");

        Assert.assertTrue(sql.contains(expectedSql));
    }

    @Test
    public void testAssociationDaoSchemaConstraints(){
        Schema schema = new Schema(
                new DaoDescriptor[]{ },
                new AssociationDaoBuilder[]{ MediaDaoBuilders.ASSOCIATION_DAO_BUILDER });

        String sql = SimpleSqlFormatter.format(schema.sql());

        String movieConstraint = SimpleSqlFormatter.format(
                "alter table actor_movie_associations add foreign key (movie_id) references movies(id);");

        String actorConstraint = SimpleSqlFormatter.format(
                "alter table actor_movie_associations add foreign key (actor_id) references actors(id);");

        Assert.assertTrue(sql.contains(movieConstraint));
        Assert.assertTrue(sql.contains(actorConstraint));
    }

}
