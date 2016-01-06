package de.obfusco.secondhand.gui.transactions;

import de.obfusco.secondhand.storage.model.Item;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ItemTableModel extends AbstractTableModel {
    private List<Item> items;

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
            case 0: return "ArtNr";
            case 1: return "Kategorie";
            case 2: return "Bezeichnung";
            case 3: return "Größe";
            case 4: return "Preis";
        }
        return super.getColumnName(column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        Item item = items.get(row);
        switch(column) {
            case 0: return item.code;
            case 1: return item.category.name;
            case 2: return item.description;
            case 3: return item.size;
            case 4: return item.price;
        }
        return null;
    }
}
