package de.obfusco.secondhand.storage.service;

import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class StorageService {

    private final static Logger LOG = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private ReservedItemRepository reservedItemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public Transaction storeSoldInformation(List<ReservedItem> items, int zipCode) {
        Date soldDate = new Date();
        Transaction transaction = Transaction.create(Transaction.Type.PURCHASE, items, zipCode);
        LOG.info("Purchase with zip code {}", zipCode);
        for (ReservedItem item : items) {
            LOG.info("Purchase of item {}", item.getCode());
            try {
                item.setSold(soldDate);
                reservedItemRepository.save(item);
            } catch (DataAccessException ex) {
                LOG.error("sold information could not be stored", ex);
            }
        }
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction storeRefundInformation(List<ReservedItem> items) {
        Transaction transaction = Transaction.create(Transaction.Type.REFUND, items, null);
        for (ReservedItem item : items) {
            LOG.info("Refund of item {}", item.getCode());
            try {
                item.setSold(null);
                reservedItemRepository.save(item);
            } catch (DataAccessException ex) {
                LOG.error("Refund information could not be stored", ex);
            }
        }
        return transactionRepository.save(transaction);
    }

    public ReservedItem getReservedItem(String code) {
        return reservedItemRepository.findByCode(code);
    }
}
