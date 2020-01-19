package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {
    Item findByCode(String code);

    List<Item> findByReservationOrderByNumberAsc(Reservation reservation);

    List<Item> findByReservationAndSoldNotNullOrderByNumberAsc(Reservation reservation);
    List<Item> findByReservationAndCheckedInNullOrderByNumberAsc(Reservation reservation);
    List<Item> findByReservationAndCheckedInNotNullAndSoldNullAndDonationFalseOrderByNumberAsc(Reservation reservation);
    List<Item> findByReservationAndSoldNullAndDonationFalseOrderByNumberAsc(Reservation reservation);
    List<Item> findByReservationAndSoldNullAndDonationTrueOrderByNumberAsc(Reservation reservation);
    List<Item> findByReservationAndCheckedInNotNullAndSoldNullAndDonationTrueOrderByNumberAsc(Reservation reservation);
    List<Item> findByReservationAndCheckedInNotNullAndSoldNullAndDonationFalseAndCheckedOutNullOrderByNumberAsc(Reservation reservation);

    List<Item> findByCheckedInNotNullAndSoldNullAndDonationFalseAndCheckedOutNullOrderByNumberAsc();

    List<Item> findByCodeIn(List<String> codes);

    long countByCheckedInNotNull();
    long countBySoldNotNull();
    long countByCheckedOutNotNull();

    long countByReservation(Reservation reservation);
    long countByReservationAndSoldNotNull(Reservation reservation);
    long countByReservationAndCheckedInNotNull(Reservation reservation);
    long countByReservationAndCheckedInNotNullAndSoldNullAndDonationFalse(Reservation reservation);
    long countByReservationAndCheckedInNotNullAndSoldNullAndDonationTrue(Reservation reservation);
    long countByReservationAndCheckedOutNotNull(Reservation reservation);
    long countByReservationAndCheckedInNotNullAndSoldNullAndDonationFalseAndCheckedOutNull(Reservation reservation);

    @Query("select sum(i.price) from items i where i.sold is not null")
    Double sumOfSoldItems();

    @Query("select i from items i join i.category c " +
            "where (lower(i.code) like lower(?1) or lower(i.description) like lower(?1) or lower(i.size) like lower(?1) or lower(c.name) like lower(?1))")
    List<Item> findByKeywords(String keyword);
    @Query("select i from items i join i.category c " +
            "where (lower(i.code) like lower(?1) or lower(i.description) like lower(?1) or lower(i.size) like lower(?1) or lower(c.name) like lower(?1)) " +
            "and (lower(i.code) like lower(?2) or lower(i.description) like lower(?2) or lower(i.size) like lower(?2) or lower(c.name) like lower(?2))")
    List<Item> findByKeywords(String keyword1, String keyword2);
    @Query("select i from items i join i.category c " +
            "where (lower(i.code) like lower(?1) or lower(i.description) like lower(?1) or lower(i.size) like lower(?1) or lower(c.name) like lower(?1)) " +
            "and (lower(i.code) like lower(?2) or lower(i.description) like lower(?2) or lower(i.size) like lower(?2) or lower(c.name) like lower(?2)) " +
            "and (lower(i.code) like lower(?3) or lower(i.description) like lower(?3) or lower(i.size) like lower(?3) or lower(c.name) like lower(?3))")
    List<Item> findByKeywords(String keyword1, String keyword2, String keyword3);
}
