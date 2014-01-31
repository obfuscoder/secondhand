package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Item;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, Integer> {

}
