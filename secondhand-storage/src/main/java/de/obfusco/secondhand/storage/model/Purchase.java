package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "purchases")
public class Purchase extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "reserved_item_id")
    ReservedItem reservedItem;

    @ManyToOne
    Customer customer;

    public static Purchase create(ReservedItem reservedItem, Customer customer) {
        Purchase purchase = new Purchase();
        purchase.setCustomer(customer);
        purchase.setReservedItem(reservedItem);
        return purchase;
    }

    public ReservedItem getReservedItem() {
        return reservedItem;
    }

    public void setReservedItem(ReservedItem reservedItem) {
        this.reservedItem = reservedItem;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
