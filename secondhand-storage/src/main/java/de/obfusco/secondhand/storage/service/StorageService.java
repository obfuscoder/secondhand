package de.obfusco.secondhand.storage.service;

import de.obfusco.secondhand.storage.model.*;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.StockItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/*
    This service encapsulates major updates into the storage. We need to be thread-safe as well as transactional.
    This implementation here deals with thread-safety and encapsulates the TransactionalStorageService which provides
    transactional context via @Transactional annotation and thus transactional proxies automatically created by JPA.

    Thread-safety is necessary as we do have the main gui thread but also one thread per peer as well as one for
    path synchronization. When the program is loading and announcing into the network, all other already running peers
    will connect almost at the same time and there are race conditions guaranteed which need to be handled with locks.

    The read methods here are considered thread safe.
 */
@Service
public class StorageService {

    private final Object transactionLock = new Object();

    @Autowired
    private TransactionalStorageService transactionalStorageService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private EventRepository eventRepository;

    public Transaction storeSoldInformation(List<String> itemCodes, String zipCode) {
        synchronized (transactionLock) {
            return transactionalStorageService.storeSoldInformation(itemCodes, zipCode);
        }
    }

    public Transaction storeRefundInformation(List<String> itemCodes) {
        synchronized (transactionLock) {
            return transactionalStorageService.storeRefundInformation(itemCodes);
        }
    }

    public Transaction checkInItems(List<Item> items) {
        synchronized (transactionLock) {
            return transactionalStorageService.checkInItems(items);
        }
    }

    public Transaction checkOutItems(List<Item> items) {
        synchronized (transactionLock) {
            return transactionalStorageService.checkOutItems(items);
        }
    }

    public Transaction parseTransactionMessageAndCreateTransaction(String message) {
        synchronized (transactionLock) {
            return transactionalStorageService.parseTransactionMessageAndCreateTransaction(message);
        }
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
        BigDecimal priceFactor = eventRepository.find().priceFactor;
        if (priceFactor != null) {
            itemSum *= priceFactor.doubleValue();
        }
        return itemSum;
    }

    public double sumOfSoldStockItems() {
        Stream<StockItem> stream = StreamSupport.stream(stockItemRepository.findAll().spliterator(), false);
        Optional<BigDecimal> stockItemSum = stream.map(it -> it.price.multiply(BigDecimal.valueOf(it.getSold()))).reduce(BigDecimal::add);
        return stockItemSum.map(BigDecimal::doubleValue).orElse(0.0);
    }

    public boolean canBeSold(BaseItem baseItem) {
        if (baseItem instanceof StockItem) return baseItem.isAvailable();
        if (baseItem instanceof Item) {
            Item item = (Item) baseItem;
            Event event = eventRepository.find();
            return item.isAvailable() && (!event.gates || item.wasCheckedIn() && !item.wasCheckedOut());
        }
        throw new RuntimeException("Unbekannter Artikeltyp!");
    }

    public boolean canRefund(BaseItem baseItem) {
        if (baseItem instanceof StockItem) return baseItem.canRefund();
        if (baseItem instanceof Item) {
            Item item = (Item) baseItem;
            Event event = eventRepository.find();
            return item.canRefund() && (!event.gates || item.wasCheckedIn() && !item.wasCheckedOut());
        }
        throw new RuntimeException("Unbekannter Artikeltyp!");
    }

    public boolean isEventGated() {
        Event event = eventRepository.find();
        return event != null && event.gates;
    }
}
