package de.obfusco.secondhand.storage.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

@Entity(name = "items")
public class Item extends AbstractEntityWithId {

    @ManyToOne
    public Category category;
    public String description;
    public String size;
    public BigDecimal price;
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    public Reservation reservation;
    public int number;
    public String code;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date sold;
    public boolean donation;
}
