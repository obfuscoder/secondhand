package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;

@Entity(name = "events")
public class Event extends AbstractEntity {

    private String name;

    public String getName() {
        return name;
    }
}
