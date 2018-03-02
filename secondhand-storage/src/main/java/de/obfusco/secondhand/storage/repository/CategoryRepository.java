package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryRepository extends CrudRepository<Category, Integer> {
    List<Category> findAllByOrderByNameAsc();
}
