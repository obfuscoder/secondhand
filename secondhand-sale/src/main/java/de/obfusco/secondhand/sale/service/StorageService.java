package de.obfusco.secondhand.sale.service;

import java.util.Date;
import java.util.List;

import de.obfusco.secondhand.storage.model.Customer;
import de.obfusco.secondhand.storage.model.Purchase;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.CustomerRepository;
import de.obfusco.secondhand.storage.repository.PurchaseRepository;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class StorageService {

    private final static Logger LOG = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private ReservedItemRepository reservedItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    public void storeSoldInformation(List<ReservedItem> items, int zipCode) {
        Date soldDate = new Date();
        Customer customer = Customer.create(zipCode);
        LOG.info("Purchase with zip code {}", zipCode);
        customerRepository.save(customer);
        for (ReservedItem item : items) {
            LOG.info("Purchase of item {}", item.getCode());
            try {
                item.setSold(soldDate);
                reservedItemRepository.save(item);
                purchaseRepository.save(Purchase.create(item, customer));
            } catch (DataAccessException ex) {
                LOG.error("sold information could not be stored", ex);
            }
        }
    }

    public ReservedItem getReservedItem(String code) {
        return reservedItemRepository.findByCode(code);
    }
}
