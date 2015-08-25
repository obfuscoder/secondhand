package de.obfusco.secondhand.dto;

import java.math.BigDecimal;
import java.util.List;

public class Event {
    public int id;
    public String name;
    public BigDecimal pricePrecision;
    public BigDecimal commissionRate;
    public BigDecimal sellerFee;
    public boolean donationOfUnsoldItemsEnabled;
    public List<Category> categories;
    public List<Reservation> reservations;
}
