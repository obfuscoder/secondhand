package de.obfusco.secondhand.storage.service;

import de.obfusco.secondhand.storage.model.BaseItem;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.StockItem;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.StockItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class StorageService {

    private final static Logger LOG = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public Transaction storeSoldInformation(List<String> itemCodes, String zipCode) {
        LOG.info("SALE: {}", itemCodes);
        return createTransaction(Transaction.Type.PURCHASE, itemCodes, zipCode);
    }

    private void saveItem(BaseItem item) {
        if (item instanceof Item) {
            itemRepository.save((Item) item);
        }
        if (item instanceof StockItem) {
            stockItemRepository.save((StockItem) item);
        }
    }

    @Transactional
    public Transaction storeRefundInformation(List<String> itemCodes) {
        LOG.info("REFUND: {}", itemCodes);
        return createTransaction(Transaction.Type.REFUND, itemCodes, null);
    }

    public Transaction parseTransactionMessage(String message) {
        String[] messageParts = message.split(";");
        if (messageParts.length != 5) {
            throw new IllegalArgumentException("Message does not contain 5 segments separated by ';'");
        }
        String id = messageParts[0];
        Transaction.Type type = Transaction.Type.valueOf(messageParts[1]);
        Date date = new Date(Long.parseLong(messageParts[2]));
        String zipCode = messageParts[3];
        String[] itemCodes = messageParts[4].split(",");
        if (transactionRepository.exists(id)) {
            LOG.debug("Skipping transaction {} as it is already known", id);
            return null;
        }
        return createTransaction(id, type, date, Arrays.asList(itemCodes), zipCode);
    }


    private Transaction createTransaction(String id, Transaction.Type type, Date date, List<String> itemCodes, String zipCode) {
        List<BaseItem> items = fetchAndUpdateItemsFromCodes(type, itemCodes);
        if (items.isEmpty()) return null;
        return Transaction.create(id, date, type, items, zipCode);
    }

    private Transaction createTransaction(Transaction.Type type, List<String> itemCodes, String zipCode) {
        List<BaseItem> items = fetchAndUpdateItemsFromCodes(type, itemCodes);
        if (items.isEmpty()) return null;
        return transactionRepository.save(Transaction.create(type, items, zipCode));
    }

    private List<BaseItem> fetchAndUpdateItemsFromCodes(Transaction.Type type, List<String> itemCodes) {
        List<BaseItem> items = new ArrayList<>();
        for (String itemCode : itemCodes) {
            BaseItem item = getItem(itemCode);
            if (item == null) {
                continue;
            }
            sellOrRefund(type, item);
            items.add(item);
        }
        return items;
    }

    private void sellOrRefund(Transaction.Type type, BaseItem item) {
        switch (type) {
            case PURCHASE:
                item.sold();
                break;
            case REFUND:
                item.refund();
                break;
        }
        saveItem(item);
    }

    public BaseItem getItem(String code) {
        Item item = itemRepository.findByCode(code);
        if (item != null) return item;
        return stockItemRepository.findByCode(code);
    }


    public double sumOfSoldItems() {
        Double itemSum = itemRepository.sumOfSoldItems();
        if (itemSum == null) itemSum = 0.0;
        itemSum += sumOfSoldStockItems();
        return itemSum;
    }

    public double sumOfSoldStockItems() {
        Stream<StockItem> stream = StreamSupport.stream(stockItemRepository.findAll().spliterator(), false);
        Optional<BigDecimal> stockItemSum = stream.map(it -> it.price.multiply(BigDecimal.valueOf(it.getSold()))).reduce(BigDecimal::add);
        return stockItemSum.map(BigDecimal::doubleValue).orElse(0.0);
    }
}
