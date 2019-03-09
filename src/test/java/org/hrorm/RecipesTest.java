package org.hrorm;

import org.hrorm.examples.Recipes;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;

public class RecipesTest {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("recipes");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb() { helper.dropSchema(); }

    @Test
    public void testValidate(){
        Connection connection  = helper.connect();

        Validator.validate(connection, Recipes.authorDaoBuilder);
        Validator.validate(connection, Recipes.recipeDaoBuilder);
        Validator.validate(connection, Recipes.ingredientDaoBuilder);
    }

}
