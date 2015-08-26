package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Transaction;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, String> {
}
