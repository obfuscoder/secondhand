package de.obfusco.secondhand.storage.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity(name = "items")
public class Item extends AbstractEntityWithId {

    @ManyToOne
    private Category category;
    private String description;
    private String size;
    private BigDecimal price;

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
