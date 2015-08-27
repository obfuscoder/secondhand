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

    public Item getItem(String code) {
        return itemRepository.findByCode(code);
    }
}
