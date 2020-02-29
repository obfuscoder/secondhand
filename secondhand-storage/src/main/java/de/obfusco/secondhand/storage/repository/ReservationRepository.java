package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ReservationRepository extends CrudRepository<Reservation, Integer> {
    Reservation findByNumber(int number);
    List<Reservation> findAllByOrderByNumberAsc();
    @Query("select r from reservations r join r.seller s order by s.lastName, s.firstName")
    List<Reservation> findAllByOrderBySellerAsc();
}
