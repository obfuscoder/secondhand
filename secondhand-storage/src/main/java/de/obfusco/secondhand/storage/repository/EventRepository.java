package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Event;

import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Integer> {

}
