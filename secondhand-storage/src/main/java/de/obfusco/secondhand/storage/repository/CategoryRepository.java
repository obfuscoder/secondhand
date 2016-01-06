package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Integer> {

}
