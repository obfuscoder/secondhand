package de.obfusco.secondhand.storage.service;

import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class StorageService {

    private final static Logger LOG = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ItemLearner itemLearner;

    @Transactional
    public Transaction storeSoldInformation(List<Item> items, String zipCode) {
        Date soldDate = new Date();
        LOG.info("SALE: {}", getItemCodes(items));
        Transaction transaction = Transaction.create(Transaction.Type.PURCHASE, items, zipCode);
        for (Item item : items) {
            try {
                item.sold = soldDate;
                itemRepository.save(item);
            } catch (DataAccessException ex) {
                LOG.error("sold information could not be stored", ex);
            }
        }
        return transactionRepository.save(transaction);
    }

    private String getItemCodes(List<Item> items) {
        List<String> codes = new ArrayList<String>();
        for(Item item : items) {
            codes.add(item.code);
        }
        return StringUtils.join(codes);
    }

    @Transactional
    public Transaction storeRefundInformation(List<Item> items) {
        LOG.info("REFUND: {}", getItemCodes(items));
        Transaction transaction = Transaction.create(Transaction.Type.REFUND, items, null);
        for (Item item : items) {
            try {
                item.sold = null;
                itemRepository.save(item);
            } catch (DataAccessException ex) {
                LOG.error("Refund information could not be stored", ex);
            }
        }
        return transactionRepository.save(transaction);
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
        return createTransaction(id, type, date, zipCode, itemCodes);
    }


    public Transaction createTransaction(String id, Transaction.Type type, Date date, String zipCode,
                                         String[] itemCodes) {
        List<Item> items = new ArrayList<>();
        for (String itemCode : itemCodes) {
            Item item = getItem(itemCode);
            if (item == null) {
                continue;
            }
            switch (type) {
                case PURCHASE:
                    item.sold = date;
                    break;
                case REFUND:
                    item.sold = null;
                    break;
            }
            items.add(item);
        }
        if (items.isEmpty()) return null;
        return Transaction.create(id, date, type, items, zipCode);
    }

    public Item getItem(String code) {
        Item item = itemRepository.findByCode(code);
        if (item == null) {
            return itemLearner.learn(code);
        }
        return item;
    }
}
