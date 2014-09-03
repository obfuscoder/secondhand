package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;

@Entity(name = "categories")
public class Category extends AbstractEntityWithId {

    private String name;

    public String getName() {
        return name;
    }
}
