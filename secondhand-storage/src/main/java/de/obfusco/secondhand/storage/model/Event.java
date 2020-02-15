package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "events")
public class Event extends AbstractEntityWithId {

    public int number;
    public String name;
    public String token;
    public BigDecimal commissionRate;
    public boolean preciseBillAmounts;
    public boolean donationOfUnsoldItemsEnabled;
    public BigDecimal pricePrecision;
    public BigDecimal sellerFee;
    public BigDecimal priceFactor;
    public boolean reservationFeesPayedInAdvance;
    public boolean gates;

    public boolean incorporateReservationFee() {
        return !reservationFeesPayedInAdvance;
    }
}
