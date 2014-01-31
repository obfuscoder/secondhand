package de.obfusco.secondhand.storage.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "customers")
public class Customer extends AbstractEntity {

    @Column(name = "zip_code")
    private Integer zipCode;

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public static Customer create(Integer zipCode) {
        Customer customer = new Customer();
        customer.setZipCode(zipCode);
        return customer;
    }
}
