package de.obfusco.secondhand.storage.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "sellers")
public class Seller extends AbstractEntity {

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
