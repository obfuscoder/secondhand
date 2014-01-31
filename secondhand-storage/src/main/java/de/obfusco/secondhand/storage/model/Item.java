package de.obfusco.secondhand.storage.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity(name = "items")
public class Item extends AbstractEntity {

    @ManyToOne
    Category category;

    private String description;

    private String size;
    BigDecimal price;

    //private Currency price;
    public String getDescription() {
        return description;
    }

    public String getSize() {
        return size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

}
