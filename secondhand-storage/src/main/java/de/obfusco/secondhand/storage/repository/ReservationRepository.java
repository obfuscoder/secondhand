package de.obfusco.secondhand.storage.repository;

import java.util.List;

import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Reservation;

import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<Reservation, Integer> {

    public Reservation findByEventAndNumber(Event event, int number);

    public List<Reservation> findByEvent(Event event);
}
