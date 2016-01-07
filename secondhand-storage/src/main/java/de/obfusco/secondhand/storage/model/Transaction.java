package de.obfusco.secondhand.storage.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "transactions")
public class Transaction extends AbstractEntityWithUuid {

    @Column(name = "zip_code")
    public String zipCode;
    @Enumerated(EnumType.STRING)
    public Type type;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name="transaction_items",
            joinColumns={@JoinColumn(name = "transaction_id", referencedColumnName = "id")},
            inverseJoinColumns ={@JoinColumn(name = "reserved_item_id", referencedColumnName = "id")}
    )
    public List<Item> items;

    public static Transaction create(Type type, List<Item> items, String zipCode) {
        Transaction transaction = new Transaction();
        transaction.type = type;
        transaction.zipCode = zipCode;
        transaction.items = items;
        return transaction;
    }

    public static Transaction create(String id, Date date, Type type, List<Item> items, String zipCode) {
        Transaction transaction = create(type, items, zipCode);
        transaction.id = id;
        transaction.created = date;
        return transaction;
    }

    public enum Type {
        PURCHASE {
            @Override
            public String toString() {
                return "Verkauf";
            }
        },
        REFUND {
            @Override
            public String toString() {
                return "Storno";
            }
        }
    }
}
