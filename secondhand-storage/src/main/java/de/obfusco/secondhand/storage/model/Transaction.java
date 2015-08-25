package de.obfusco.secondhand.storage.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Date;
import java.util.List;

@Entity(name = "transactions")
public class Transaction extends AbstractEntityWithUuid {

    public enum Type {
        PURCHASE,
        REFUND
    }

    @Column(name = "zip_code")
    private Integer zipCode;
    @Enumerated(EnumType.STRING)
    private Type type;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name="transaction_items",
            joinColumns={@JoinColumn(name = "transaction_id", referencedColumnName = "id")},
            inverseJoinColumns ={@JoinColumn(name = "reserved_item_id", referencedColumnName = "id")}
    )
    private List<Item> items;

    public Integer getZipCode() {
        return zipCode;
    }

    public Type getType() {
        return type;
    }

    public List<Item> getItems() {
        return items;
    }


    public static Transaction create(Type type, List<Item> items, Integer zipCode) {
        Transaction transaction = new Transaction();
        transaction.type = type;
        transaction.zipCode = zipCode;
        transaction.items = items;
        return transaction;
    }

    public static Transaction create(String id, Date date, Type type, List<Item> items, Integer zipCode) {
        Transaction transaction = create(type, items, zipCode);
        transaction.setId(id);
        transaction.setCreated(date);
        return transaction;
    }

}
