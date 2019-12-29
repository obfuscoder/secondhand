package de.obfusco.secondhand.storage.model;

import javax.persistence.Index;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import java.math.BigDecimal;

@MappedSuperclass
@Table(indexes = {@Index(columnList = "code", unique = true)})
public abstract class BaseItem extends AbstractEntityWithId {

    public String description;
    public BigDecimal price;
    public int number;
    public String code;

    public abstract void sold();

    public abstract void refund();

    public abstract String getCategoryName();

    public abstract boolean isAvailable();

    public abstract String getSize();

    public abstract boolean canRefund();

    public abstract boolean isUnique();

    public abstract void checkIn();

    public abstract void checkOut();
}
