package de.obfusco.secondhand.storage.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

@Entity(name = "reserved_items")
public class ReservedItem extends AbstractEntity {

    @ManyToOne
    Item item;

    String code;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date sold;

    public String getCode() {
        return code;
    }

    public Item getItem() {
        return item;
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
