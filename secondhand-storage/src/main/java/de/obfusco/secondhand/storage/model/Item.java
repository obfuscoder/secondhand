package de.obfusco.secondhand.storage.model;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "items")
public class Item extends BaseItem {

    public enum Gender {
        MALE, FEMALE, BOTH
    }

    @ManyToOne
    public Category category;
    public String size;

    @Enumerated
    public Gender gender;
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    public Reservation reservation;

    public boolean donation;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date sold;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date checkedIn;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date checkedOut;

    @Override
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
    public boolean isAvailable() {
        return sold == null;
    }

    @Transient
    public boolean wasSold() {
        return sold != null;
    }

    @Transient
    public boolean wasCheckedIn() {
        return checkedIn != null;
    }

    @Transient
    public boolean wasCheckedOut() {
        return checkedOut != null;
    }

    @Override
    public void checkIn() {
        checkedIn = new Date();
    }

    @Override
    public void checkOut() {
        checkedOut = new Date();
    }

}
