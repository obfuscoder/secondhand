package de.obfusco.secondhand.gui.checkin;

import de.obfusco.secondhand.storage.model.Item;

import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

class ItemTableModel extends AbstractTableModel {
    private List<Item> items;

    private NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    public ItemTableModel(List<Item> items) {
        this.items = items;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Nummer";
            case 1:
                return "Kategorie";
            case 2:
                return "Beschreibung";
            case 3:
                return "Preis";
            case 4:
                return "eingecheckt";
        }
        return super.getColumnName(column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        Item item = items.get(row);
        switch (column) {
            case 0:
                return item.number;
            case 1:
                return item.category.name;
            case 2:
                return item.description;
            case 3:
                return currency.format(item.price);
            case 4:
                return item.checkedIn ? "ja" : "nein";
        }
        return null;
    }

    public int indexOf(Integer id) {
        for(int i=0; i<items.size(); i++)
            if (items.get(i).getId().equals(id))
                return i;
        return -1;
    }

    public int update(Item item) {
        for(int i=0; i<items.size(); i++)
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                fireTableRowsUpdated(i, i);
                return i;
            }
        return -1;
    }
}
