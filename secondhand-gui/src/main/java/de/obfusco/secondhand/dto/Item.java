package de.obfusco.secondhand.dto;

import java.math.BigDecimal;
import java.sql.Date;

public class Item {
    public int id;
    public int categoryId;
    public String description;
    public String size;
    public BigDecimal price;
    public int number;
    public String code;
    public Date sold;
    public boolean donation;
}
