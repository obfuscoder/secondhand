package de.obfusco.secondhand.storage.model;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity(name = "transactions")
public class Transaction extends AbstractEntityWithUuid {

    @Column(name = "zip_code")
    private String zipCode;
    @Enumerated(EnumType.STRING)
    public Type type;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name="transaction_items",
            joinColumns={@JoinColumn(name = "transaction_id", referencedColumnName = "id")},
            inverseJoinColumns ={@JoinColumn(name = "item_id", referencedColumnName = "id")}
    )
    private List<Item> items;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="transaction_stock_items",
            joinColumns={@JoinColumn(name = "transaction_id", referencedColumnName = "id")},
            inverseJoinColumns ={@JoinColumn(name = "stock_item_id", referencedColumnName = "id")}
    )
    private List<StockItem> stockItems;


    public static Transaction create(Type type, List<? extends BaseItem> items, String zipCode) {
        Transaction transaction = new Transaction();
        transaction.type = type;
        transaction.zipCode = zipCode;
        transaction.items = items.stream().filter(it -> it instanceof Item).map(it -> (Item) it).collect(Collectors.toList());
        transaction.stockItems = items.stream().filter(it -> it instanceof StockItem).map(it -> (StockItem) it).collect(Collectors.toList());
        return transaction;
    }

    public static Transaction create(String id, Date date, Type type, List<BaseItem> items, String zipCode) {
        Transaction transaction = create(type, items, zipCode);
        transaction.id = id;
        transaction.created = date;
        return transaction;
    }

    public String toString() {
        return id + ";" +
                type.name() + ";" +
                created.getTime() + ";" +
                zipCode + ";" +
                StringUtils.arrayToCommaDelimitedString(getAllItemCodes().toArray());
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setStockItems(List<StockItem> stockItems) {
        this.stockItems = stockItems;
    }

    public List<String> getAllItemCodes() {
        return getAllItems().stream().map(it -> it.code).collect(Collectors.toList());
    }

    @Transient
    public List<BaseItem> getAllItems() {
        List<BaseItem> allItems = new ArrayList<>();
        allItems.addAll(items);
        allItems.addAll(stockItems);
        return allItems;
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
