package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Purchase;

import org.springframework.data.repository.CrudRepository;

public interface PurchaseRepository extends CrudRepository<Purchase, Integer> {

}
