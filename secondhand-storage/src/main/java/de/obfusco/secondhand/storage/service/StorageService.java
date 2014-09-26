package de.obfusco.secondhand.storage.service;

import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import org.apache.commons.lang3.ArrayUtils;
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
    private ReservedItemRepository reservedItemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public Transaction storeSoldInformation(List<ReservedItem> items, int zipCode) {
        Date soldDate = new Date();
        LOG.info("SALE: {}", getItemCodes(items));
        Transaction transaction = Transaction.create(Transaction.Type.PURCHASE, items, zipCode);
        for (ReservedItem item : items) {
            try {
                item.setSold(soldDate);
                reservedItemRepository.save(item);
            } catch (DataAccessException ex) {
                LOG.error("sold information could not be stored", ex);
            }
        }
        return transactionRepository.save(transaction);
    }

    private String getItemCodes(List<ReservedItem> items) {
        List<String> codes = new ArrayList<String>();
        for(ReservedItem item : items) {
            codes.add(item.getCode());
        }
        return StringUtils.join(codes);
    }

    @Transactional
    public Transaction storeRefundInformation(List<ReservedItem> items) {
        LOG.info("REFUND: {}", getItemCodes(items));
        Transaction transaction = Transaction.create(Transaction.Type.REFUND, items, null);
        for (ReservedItem item : items) {
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
