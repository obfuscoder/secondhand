package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import java.util.Date;

@Entity(name = "reserved_items")
public class ReservedItem extends AbstractEntityWithId {

    @ManyToOne
    private Item item;
    @ManyToOne
    private Reservation reservation;
    private String code;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sold;
    private int number;

    public String getCode() {
        return code;
    }

    public Item getItem() {
        return item;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public boolean isSold() {
        return getSold() != null;
    }

    public Date getSold() {
        return sold;
    }

    public void setSold(Date sold) {
        this.sold = sold;
    }

    public int getNumber() {
        return number;
    }
}
