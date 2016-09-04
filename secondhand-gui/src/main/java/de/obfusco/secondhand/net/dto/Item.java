package de.obfusco.secondhand.net.dto;

import java.math.BigDecimal;
import java.sql.Date;

public class Item {
    public int categoryId;
    public int reservationId;
    public String description;
    public String size;
    public BigDecimal price;
    public int number;
    public String code;
    public Date sold;
    public boolean donation;
    public boolean adhoc;
}
