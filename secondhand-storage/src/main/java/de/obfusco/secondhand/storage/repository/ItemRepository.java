package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

}
