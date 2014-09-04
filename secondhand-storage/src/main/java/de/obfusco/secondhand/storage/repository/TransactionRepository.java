package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, String> {
}
