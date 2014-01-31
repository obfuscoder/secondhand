package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Customer;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Integer> {

}
