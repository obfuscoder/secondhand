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
    private Category category;
    private String description;
    private String size;
    private BigDecimal price;
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
    private int number;
    private String code;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sold;

    public String getDescription() {
        return description;
    }

    public String getSize() {
        return size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public int getNumber() {
        return number;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Date getSold() {
        return sold;
    }

    public void setSold(Date sold) {
        this.sold = sold;
    }

    public boolean isSold() {
        return getSold() != null;
    }
}
