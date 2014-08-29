package de.obfusco.secondhand.storage.model;

public class ZipCodeCount {

    private int zipCode;
    private int count;

    public ZipCodeCount(int zipCode, int count) {
        this.zipCode = zipCode;
        this.count = count;
    }

    public int getZipCode() {
        return zipCode;
    }

    public int getCount() {
        return count;
    }
}
