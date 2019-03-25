package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Recipes;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class RecipesTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("recipes");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb() { helper.dropSchema(); }

    @Test
    public void testValidate() throws SQLException {
        Connection connection  = helper.connect();

        Validator.validate(connection, Recipes.authorDaoBuilder);
        Validator.validate(connection, Recipes.recipeDaoBuilder);
        Validator.validate(connection, Recipes.ingredientDaoBuilder);

        connection.close();
    }

}
