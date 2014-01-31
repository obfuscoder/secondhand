package de.obfusco.secondhand.storage.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ZipCodeCount {

    @Id
    @Column(name = "zip_code")
    private int zipCode;
    private int count;

    public int getZipCode() {
        return zipCode;
    }

    public int getCount() {
        return count;
    }
}
