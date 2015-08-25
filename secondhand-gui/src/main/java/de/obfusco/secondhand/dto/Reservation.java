package de.obfusco.secondhand.dto;

import java.util.List;

public class Reservation {
    public int id;
    public int number;
    public Seller seller;
    public List<Item> items;
}
