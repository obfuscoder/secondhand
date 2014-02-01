package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Reservation;

import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<Reservation, Integer> {

    public Reservation findByEventIdAndNumber(int eventId, int number);
}
