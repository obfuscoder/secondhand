package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.ReservedItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReservedItemRepository extends CrudRepository<ReservedItem, Integer> {

    ReservedItem findByCode(String code);

    List<ReservedItem> findByReservationOrderByCodeAsc(Reservation reservation);

    List<ReservedItem> findByReservationAndSoldNotNull(Reservation reservation);

    List<ReservedItem> findByReservationAndSoldNull(Reservation reservation);

    List<ReservedItem> findByReservationEventAndSoldNotNull(Event event);

    long countBySoldNotNull();
    @Query("select sum(i.price) from reserved_items ri join ri.item i where ri.sold is not null")
    Double sumOfSoldItems();

    @Query("select ri from reserved_items ri join ri.item i join i.category c " +
            "where (lower(ri.code) like lower(?1) or lower(i.description) like lower(?1) or lower(i.size) like lower(?1) or lower(c.name) like lower(?1))")
    List<ReservedItem> findByKeywords(String keyword);
    @Query("select ri from reserved_items ri join ri.item i join i.category c " +
            "where (lower(ri.code) like lower(?1) or lower(i.description) like lower(?1) or lower(i.size) like lower(?1) or lower(c.name) like lower(?1)) " +
            "and (lower(ri.code) like lower(?2) or lower(i.description) like lower(?2) or lower(i.size) like lower(?2) or lower(c.name) like lower(?2))")
    List<ReservedItem> findByKeywords(String keyword1, String keyword2);
    @Query("select ri from reserved_items ri join ri.item i join i.category c " +
            "where (lower(ri.code) like lower(?1) or lower(i.description) like lower(?1) or lower(i.size) like lower(?1) or lower(c.name) like lower(?1)) " +
            "and (lower(ri.code) like lower(?2) or lower(i.description) like lower(?2) or lower(i.size) like lower(?2) or lower(c.name) like lower(?2)) " +
            "and (lower(ri.code) like lower(?3) or lower(i.description) like lower(?3) or lower(i.size) like lower(?3) or lower(c.name) like lower(?3))")
    List<ReservedItem> findByKeywords(String keyword1, String keyword2, String keyword3);
}
