package de.obfusco.secondhand.storage.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "sellers")
public class Seller extends AbstractEntity {

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String street;
    @Column(name = "zip_code")
    private Integer zipCode;
    private String city;
    private String email;
    private String phone;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return getFirstName() + " " + getLastName();
    }

    public String getStreet() {
        return street;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
