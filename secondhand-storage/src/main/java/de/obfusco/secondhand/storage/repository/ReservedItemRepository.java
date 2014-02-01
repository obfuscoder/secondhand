package de.obfusco.secondhand.storage.repository;

import java.util.List;

import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.ReservedItem;

import org.springframework.data.repository.CrudRepository;

public interface ReservedItemRepository extends CrudRepository<ReservedItem, Integer> {

    ReservedItem findByCode(String code);

    List<ReservedItem> findByReservation(Reservation reservation);
}
