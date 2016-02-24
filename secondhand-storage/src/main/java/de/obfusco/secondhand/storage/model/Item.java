package de.obfusco.secondhand.storage.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "items")
@Table(indexes = {@Index(columnList = "code", unique = true)})
public class Item extends AbstractEntityWithId {

    @ManyToOne
    public Category category;
    public String description;
    public String size;
    public BigDecimal price;
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    public Reservation reservation;
    public int number;
    public String code;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date sold;
    public boolean donation;
}
