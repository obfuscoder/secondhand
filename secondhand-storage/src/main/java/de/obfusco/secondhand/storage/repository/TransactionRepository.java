package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, String> {
}
