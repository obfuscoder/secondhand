package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.StockItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StockItemRepository extends CrudRepository<StockItem, Integer> {
    StockItem findByCode(String code);
    List<StockItem> findByCodeIn(List<String> codes);

    @Query("select sum(si.sold) from stock_items si")
    Long countOfSoldItems();
}
