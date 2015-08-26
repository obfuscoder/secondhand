package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface EventRepository extends CrudRepository<Event, Integer> {
    @Query("select e from events e")
    Event find();
}
