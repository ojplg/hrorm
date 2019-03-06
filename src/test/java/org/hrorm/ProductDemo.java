package org.hrorm;

import org.hrorm.examples.Product;
import org.hrorm.examples.ProductCategory;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDemo {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("product");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    private DaoBuilder<Product> daoBuilder(){
        return new DaoBuilder<>("products", Product::new)
                .withPrimaryKey("id", "products_sequence", Product::getId, Product::setId)
                .withStringColumn("name", Product::getName, Product::setName)
                .withConvertingStringColumn("category", Product::getCategory, Product::setCategory, new CategoryConverter())
                .withBigDecimalColumn("price", Product::getPrice, Product::setPrice)
                .withIntegerColumn("sku", Product::getSku, Product::setSku)
                .withBooleanColumn("discontinued", Product::isDiscontinued, Product::setDiscontinued)
                .withLocalDateTimeColumn("first_available", Product::getFirstAvailable, Product::setFirstAvailable);
    }

    class CategoryConverter implements Converter<ProductCategory, String> {
        @Override
        public String from(ProductCategory item) {
            return item.toString();
        }

        @Override
        public ProductCategory to(String aString) {
            return ProductCategory.valueOf(aString);
        }
    }

    @Test
    public void testInsertAndSelect(){

        long id;
        {
            Dao<Product> dao = daoBuilder().buildDao(helper.connect());

            Product product = new Product();
            product.setName("Chef Knife");
            product.setCategory(ProductCategory.Kitchen);
            product.setPrice(new BigDecimal("99.95"));
            product.setSku(12345L);
            product.setDiscontinued(false);
            product.setFirstAvailable(LocalDateTime.of(2017, 6, 15, 0, 0));

            id = dao.insert(product);
        }
        {
            Dao<Product> dao = daoBuilder().buildDao(helper.connect());
            Product product = dao.select(id);

            Assert.assertEquals(ProductCategory.Kitchen, product.getCategory());
        }

    }

}
