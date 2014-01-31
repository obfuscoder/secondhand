package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "reservations")
public class Reservation extends AbstractEntity {

    Integer number;

    @ManyToOne
    @JoinColumn(name = "event_id")
    Event event;

    @ManyToOne
    Seller seller;

    public Integer getNumber() {
        return number;
    }

    public Event getEvent() {
        return event;
    }
}
