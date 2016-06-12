package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity(name = "reservations")
public class Reservation extends AbstractEntityWithId {

    public int number;
    @ManyToOne
    public Seller seller;
    public BigDecimal commissionRate;
    public BigDecimal fee;
}
