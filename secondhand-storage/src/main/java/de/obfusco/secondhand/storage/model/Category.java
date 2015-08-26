package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;

@Entity(name = "categories")
public class Category extends AbstractEntityWithId {
    public String name;
}
