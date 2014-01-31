package de.obfusco.secondhand.sale.service;

import java.util.Date;
import java.util.List;

import de.obfusco.secondhand.storage.model.Customer;
import de.obfusco.secondhand.storage.model.Purchase;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.CustomerRepository;
import de.obfusco.secondhand.storage.repository.PurchaseRepository;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class StorageService {

    @Autowired
    private ReservedItemRepository reservedItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    public void storeSoldInformation(List<String> itemCodes, int zipCode) {
        Date soldDate = new Date();
        Customer customer = Customer.create(zipCode);
        customerRepository.save(customer);
        for (String code : itemCodes) {
            try {
                ReservedItem item = reservedItemRepository.findByCode(code.toString());
                item.setSold(soldDate);
                reservedItemRepository.save(item);
                purchaseRepository.save(Purchase.create(item, customer));
            } catch (DataAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    public ReservedItem getReservedItem(String code) {
        return reservedItemRepository.findByCode(code);
    }
}
