package de.obfusco.secondhand.net.dto;

import java.math.BigDecimal;
import java.util.List;

public class Event {
    public int id;
    public String name;
    public String token;
    public BigDecimal pricePrecision;
    public BigDecimal commissionRate;
    public BigDecimal sellerFee;
    public boolean donationOfUnsoldItemsEnabled;
    public boolean reservationFeesPayedInAdvance;
    public List<Category> categories;
    public List<Seller> sellers;
    public List<Reservation> reservations;
    public List<Item> items;
}
