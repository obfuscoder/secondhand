package de.obfusco.secondhand.storage.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "sellers")
public class Seller extends AbstractEntityWithId {

    @Column(name = "first_name")
    public String firstName;
    @Column(name = "last_name")
    public String lastName;
    public String street;
    @Column(name = "zip_code")
    public Integer zipCode;
    public String city;
    public String email;
    public String phone;

    public String getName() {
        return firstName + " " + lastName;
    }
}
