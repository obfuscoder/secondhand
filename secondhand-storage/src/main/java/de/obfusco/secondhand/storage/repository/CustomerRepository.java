package de.obfusco.secondhand.storage.repository;

import java.util.List;

import de.obfusco.secondhand.storage.model.Customer;
import de.obfusco.secondhand.storage.model.ZipCodeCount;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Integer> {

    List<ZipCodeCount> getZipCodeCounts();
}
