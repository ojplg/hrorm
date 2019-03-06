package org.hrorm;

import net.bytebuddy.asm.Advice;
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
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * This class contains demonstrations used in hrorm chapter one article.
 */
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

    @Test
    public void testSelectByColumns(){

        {
            Dao<Product> dao = daoBuilder().buildDao(helper.connect());

            for (int idx = 0; idx < 10; idx++) {
                Product product = new Product();
                product.setName("Electronic Item" + idx);
                product.setCategory(ProductCategory.Electronic);
                product.setDiscontinued(idx % 2 == 0);
                product.setPrice(new BigDecimal("100"));
                product.setSku((long) idx);
                product.setFirstAvailable(LocalDateTime.now());

                dao.insert(product);
            }
        }
        {
            Dao<Product> productDao = daoBuilder().buildDao(helper.connect());

            Product template = new Product();
            template.setCategory(ProductCategory.Electronic);
            template.setDiscontinued(false);

            List<Product> products = productDao.selectManyByColumns(template, "category", "discontinued");

            Assert.assertEquals(5, products.size());
        }
    }

    @Test
    public void testSelectWhere(){
        {
            Dao<Product> dao = daoBuilder().buildDao(helper.connect());
            LocalDateTime date = LocalDateTime.of(2017, 11, 1, 0,0 );

            for (int idx = 0; idx < 500; idx++) {

                ProductCategory productCategory = ProductCategory.values()[idx%4];

                Product product = new Product();
                product.setName(productCategory.toString() + " " + idx);
                product.setCategory(productCategory);
                product.setDiscontinued(idx % 2 == 0);
                product.setPrice(new BigDecimal("" + idx + ".95"));
                product.setSku((long) idx);
                product.setFirstAvailable(date);

                date = date.plusDays(1);

                dao.insert(product);
            }
        }
        {
            Dao<Product> productDao = daoBuilder().buildDao(helper.connect());

            List<Product> products = productDao.select(
                    new Where("category", Operator.EQUALS, ProductCategory.Miscellaneous.toString())
                            .and("price", Operator.LESS_THAN, new BigDecimal("100.00"))
                            .and("first_available", Operator.GREATER_THAN_OR_EQUALS, LocalDateTime.of(2018,1,1,0,0))
                            .and("first_available", Operator.LESS_THAN_OR_EQUALS, LocalDateTime.of(2018,12,31,23,59)));

            Assert.assertEquals(10, products.size());
        }
    }

    @Test
    public void testFolding(){
        {
            Dao<Product> dao = daoBuilder().buildDao(helper.connect());
            LocalDateTime date = LocalDateTime.of(2017, 11, 1, 0,0 );

            for (int idx = 0; idx < 500; idx++) {

                ProductCategory productCategory = ProductCategory.values()[idx%4];

                Product product = new Product();
                product.setName(productCategory.toString() + " " + idx);
                product.setCategory(productCategory);
                product.setDiscontinued(idx % 5 == 0);
                product.setPrice(new BigDecimal(idx));
                product.setSku((long) idx);
                product.setFirstAvailable(date);

                date = date.plusDays(1);

                dao.insert(product);
            }
        }
        BigDecimal foldingTotal;
        {
            Dao<Product> productDao = daoBuilder().buildDao(helper.connect());
            foldingTotal = productDao.foldingSelect(
                    new BigDecimal(0),
                    (accumulatedCost, product) -> accumulatedCost.add(product.getPrice()),
                    new Where("discontinued", Operator.EQUALS, false));

        }
        BigDecimal functionTotal;
        {
            Dao<Product> productDao = daoBuilder().buildDao(helper.connect());
            functionTotal = productDao.runBigDecimalFunction(SqlFunction.SUM,
                    "price",
                    new Where("discontinued", Operator.EQUALS, false));
        }
        Assert.assertEquals(functionTotal, foldingTotal);
        Assert.assertEquals(new BigDecimal("100000"), foldingTotal);
    }

}
