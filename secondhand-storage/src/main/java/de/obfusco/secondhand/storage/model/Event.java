package de.obfusco.secondhand.storage.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;

@Entity(name = "events")
public class Event extends AbstractEntityWithId {

    public String name;
    public String token;
    public BigDecimal commissionRate;
    public boolean donationOfUnsoldItemsEnabled;
    public BigDecimal pricePrecision;
    public BigDecimal sellerFee;
}
