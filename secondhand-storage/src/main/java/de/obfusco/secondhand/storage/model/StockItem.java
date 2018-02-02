package de.obfusco.secondhand.storage.model;

import javax.persistence.Entity;

@Entity(name = "stock_items")
public class StockItem extends BaseItem {

    private int sold;

    @Override
    public void sold() {
        sold += 1;
    }

    @Override
    public void refund() {
        if (sold > 0) sold -= 1;
    }

    @Override
    public String getCategoryName() {
        return "";
    }

    @Override
    public boolean canSell() {
        return true;
    }

    @Override
    public String getSize() {
        return "";
    }

    @Override
    public boolean canRefund() {
        return sold > 0;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }
}
