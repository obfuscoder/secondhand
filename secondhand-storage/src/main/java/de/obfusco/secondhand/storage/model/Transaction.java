package de.obfusco.secondhand.storage.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity(name = "transactions")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Transaction extends AbstractEntityWithUuid {

    public List<ReservedItem> getReservedItems() {
        return reservedItems;
    }

    public static enum Type {
        PURCHASE,
        REFUND
    }

    @Column(name = "zip_code")
    private Integer zipCode;
    @Enumerated(EnumType.STRING)
    private Type type;
    @ManyToMany
    @JoinTable(name="transaction_items",
            joinColumns={@JoinColumn(name = "transaction_id", referencedColumnName = "id")},
            inverseJoinColumns ={@JoinColumn(name = "reserved_item_id", referencedColumnName = "id")}
    )
    private List<ReservedItem> reservedItems;

    public Integer getZipCode() {
        return zipCode;
    }

    public Type getType() {
        return type;
    }

    public static Transaction create(Type type, List<ReservedItem> reservedItems, Integer zipCode) {
        Transaction transaction = new Transaction();
        transaction.type = type;
        transaction.zipCode = zipCode;
        transaction.reservedItems = reservedItems;
        return transaction;
    }
}
