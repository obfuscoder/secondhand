package de.obfusco.secondhand.storage.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

@Entity(name = "reserved_items")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class ReservedItem extends AbstractEntityWithId {

    @ManyToOne
    private Item item;
    @ManyToOne
    private Reservation reservation;
    private String code;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sold;

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
}
