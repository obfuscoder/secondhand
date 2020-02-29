package de.obfusco.secondhand.storage.service;

import de.obfusco.secondhand.storage.model.*;
import de.obfusco.secondhand.storage.repository.EventRepository;
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
import java.util.stream.Collectors;
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

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public Transaction storeSoldInformation(List<String> itemCodes, String zipCode) {
        LOG.info("SALE: {}", itemCodes);
        return createTransaction(Transaction.Type.PURCHASE, itemCodes, zipCode);
    }

    @Transactional
    public Transaction checkInItems(List<Item> items) {
        items.forEach(item -> {
            item.checkIn();
            itemRepository.save(item);
        });
        List<String> itemCodes = items.stream().map(i -> i.code).collect(Collectors.toList());
        LOG.info("CHECKIN: {}", itemCodes);
        return createTransaction(Transaction.Type.CHECKIN, itemCodes, null);
    }

    @Transactional
    public Transaction checkOutItems(List<Item> items) {
        items.forEach(item -> {
            item.checkOut();
            itemRepository.save(item);
        });
        List<String> itemCodes = items.stream().map(i -> i.code).collect(Collectors.toList());
        LOG.info("CHECKOUT: {}", itemCodes);
        return createTransaction(Transaction.Type.CHECKOUT, itemCodes, null);
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
        if (transactionRepository.existsById(id)) {
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
            updateItem(type, item);
            items.add(item);
        }
        return items;
    }

    private void updateItem(Transaction.Type type, BaseItem item) {
        switch (type) {
            case PURCHASE:
                item.sold();
                break;
            case REFUND:
                item.refund();
                break;
            case CHECKIN:
                item.checkIn();
                break;
            case CHECKOUT:
                item.checkOut();
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
