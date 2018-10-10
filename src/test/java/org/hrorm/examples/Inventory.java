package org.hrorm.examples;

import lombok.Data;
import org.hrorm.DaoBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Inventory {
    Long id;
    LocalDateTime date;
    List<Stock> stocks;

    DaoBuilder<Stock> stockDaoBuilder = new DaoBuilder<>("STOCK", Stock::new)
            .withPrimaryKey("ID","STOCK_SEQUENCE", Stock::getId, Stock::setId)
            .withIntegerColumn("INVENTORY_ID", Stock::getInventoryId, Stock::setInventoryId)
            .withStringColumn("PRODUCT_NAME", Stock::getProductName, Stock::setProductName)
            .withBigDecimalColumn("AMOUNT", Stock::getAmount, Stock::setAmount);

    DaoBuilder<Inventory> inventoryDaoBuilder = new DaoBuilder<>("INVENTORY", Inventory::new)
            .withPrimaryKey("ID", "INVENTORY_SEQUENCE", Inventory::getId, Inventory::setId)
            .withLocalDateTimeColumn("DATE", Inventory::getDate, Inventory::setDate)
            .withChildren(Inventory::getStocks, Inventory::setStocks, stockDaoBuilder);

}
