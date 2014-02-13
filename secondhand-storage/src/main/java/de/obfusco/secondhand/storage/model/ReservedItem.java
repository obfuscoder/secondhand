package de.obfusco.secondhand.storage.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

@Entity(name = "reserved_items")
public class ReservedItem extends AbstractEntity {

    @ManyToOne
    Item item;

    @ManyToOne
    Reservation reservation;

    String code;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date sold;

    @Temporal(javax.persistence.TemporalType.DATE)
    Date refunded;

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

    public Date getRefunded() {
        return refunded;
    }

    public void setRefunded(Date refunded) {
        this.refunded = refunded;
    }

    public boolean isRefunded() {
        return getRefunded() != null;
    }
}
