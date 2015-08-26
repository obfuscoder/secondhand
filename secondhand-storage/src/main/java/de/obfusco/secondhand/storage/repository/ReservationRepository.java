package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Reservation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ReservationRepository extends CrudRepository<Reservation, Integer> {
    Reservation findByNumber(int number);
}
