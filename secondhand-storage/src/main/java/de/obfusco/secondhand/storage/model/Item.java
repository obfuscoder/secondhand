package de.obfusco.secondhand.storage.model;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "items")
public class Item extends BaseItem {

    public enum Gender {
        MALE, FEMALE, BOTH
    }

    @ManyToOne
    private Category category;
    private String size;

    @Enumerated
    private Gender gender;
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

    public Gender getGender() {
        return gender;
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

    public void setSize(String size) {
        this.size = size;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setDonation(boolean donation) {
        this.donation = donation;
    }
}
