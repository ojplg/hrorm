package org.hrorm.examples;

import lombok.Data;
import org.hrorm.Dao;
import org.hrorm.DaoBuilder;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

//
// This is just demo code. Easier to write here than in the index.html.
//
public class Recipes {

    @Data
    class Author {
        Long id;
        String name;
    }

    @Data
    class Recipe {
        Long id;
        String name;
        Author author;
        List<Ingredient> ingredients;
    }

    @Data
    class Ingredient {
        Long id;
        long recipeId;
        String name;
        long amount;
    }

    DaoBuilder<Author> authorDaoBuilder = new DaoBuilder<>("AUTHOR", Author::new)
            .withPrimaryKey("ID", "AUTHOR_SEQUENCE", Author::getId, Author::setId)
            .withStringColumn("NAME", Author::getName, Author::setName);

    DaoBuilder<Ingredient> ingredientDaoBuilder = new DaoBuilder<>("INGREDIENT", Ingredient::new)
            .withPrimaryKey("ID", "INGREDIENT_SEQUENCE", Ingredient::getId, Ingredient::setId)
            .withIntegerColumn("RECIPE_ID", Ingredient::getRecipeId, Ingredient::setRecipeId)
            .withStringColumn("NAME", Ingredient::getName, Ingredient::setName);

    DaoBuilder<Recipe> recipeDaoBuilder = new DaoBuilder<>("RECIPE", Recipe::new)
            .withPrimaryKey("ID", "RECIPE_SEQUENCE", Recipe::getId, Recipe::setId)
            .withStringColumn("NAME", Recipe::getName, Recipe::setName)
            .withJoinColumn("AUTHOR_ID", Recipe::getAuthor, Recipe::setAuthor, authorDaoBuilder)
            .withChildren( Recipe::getIngredients, Recipe::setIngredients, ingredientDaoBuilder);


    void example(){
        Connection connection = null; // somehow this happened

        Dao<Author> authorDao = authorDaoBuilder.buildDao(connection);

        Author juliaChild = new Author();
        juliaChild.setName("Julia Child");

        authorDao.insert(juliaChild);

        Ingredient carrots = new Ingredient();
        carrots.setName("Carrots");
        carrots.setAmount(4L);
        Ingredient onions = new Ingredient();
        onions.setName("Onions");
        onions.setAmount(2L);
        Ingredient beef = new Ingredient();
        beef.setName("Beef");
        beef.setAmount(1L);

        Recipe beefStew = new Recipe();
        beefStew.setName("Beef stew");
        beefStew.setAuthor(juliaChild);
        beefStew.setIngredients(Arrays.asList(carrots, onions, beef));

        Dao<Recipe> recipeDao = recipeDaoBuilder.buildDao(connection);
        recipeDao.insert(beefStew);
    }

}
