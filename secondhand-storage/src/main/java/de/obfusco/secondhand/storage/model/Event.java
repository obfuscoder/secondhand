package de.obfusco.secondhand.storage.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;

@Entity(name = "events")
public class Event extends AbstractEntityWithId {

    private String name;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }
}
