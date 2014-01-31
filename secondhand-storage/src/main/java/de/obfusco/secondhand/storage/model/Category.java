package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;

@Entity(name = "categories")
public class Category extends AbstractEntity {

    private String name;

    public String getName() {
        return name;
    }
}
