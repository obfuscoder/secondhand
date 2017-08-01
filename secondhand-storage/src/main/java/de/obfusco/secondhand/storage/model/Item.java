package de.obfusco.secondhand.storage.model;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "items")
public class Item extends BaseItem {

    @ManyToOne
    private Category category;
    private String size;
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    public Date getSold() {
        return sold;
    }

    public void setSold(Date sold) {
        this.sold = sold;
    }

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sold;
    private boolean donation;

    public String getSize() {
        return size;
    }

    @Override
    public boolean canRefund() {
        return sold != null;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public boolean isDonation() {
        return donation;
    }

    public Category getCategory() {

        return category;
    }

    @Override
    public void sold() {
        sold = new Date();
    }

    @Override
    public void refund() {
        sold = null;
    }

    @Override
    public String getCategoryName() {
        return category.name;
    }

    @Override
    public boolean canSell() {
        return sold == null;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    @Transient
    public boolean wasSold() {
        return sold != null;
    }
}
