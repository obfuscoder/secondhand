package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.ReservedItem;

import org.springframework.data.repository.CrudRepository;

public interface ReservedItemRepository extends CrudRepository<ReservedItem, Integer> {

    ReservedItem findByCode(String code);
}
