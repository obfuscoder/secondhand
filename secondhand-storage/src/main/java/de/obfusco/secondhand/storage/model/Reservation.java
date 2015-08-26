package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity(name = "reservations")
public class Reservation extends AbstractEntityWithId {

    public int number;
    @ManyToOne
    public Seller seller;
}
