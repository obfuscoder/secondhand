package de.obfusco.secondhand.gui.checkout;

import de.obfusco.secondhand.storage.model.Item;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

public class CheckoutDialog extends JDialog {
    private JPanel contentPane;
    private JButton checkOutButton;
    private JTable itemTable;
    private JTextField itemCodeField;

    private static NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private Map<String, Item> itemCodeMap;
    private List<Item> items;

    private List<Item> scannedItems = new ArrayList<>();

    public CheckoutDialog(List<Item> items) {
        this.items = items;
        itemCodeMap = new HashMap<>();
        items.forEach(i -> this.itemCodeMap.put(i.code, i));
        setContentPane(contentPane);
        setModal(true);

        checkOutButton.addActionListener(e -> onClose());

        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        itemCodeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    onScan();
                }
            }
        });

        DefaultTableModel itemTableModel = (DefaultTableModel) itemTable.getModel();
        itemTableModel.setRowCount(0);
        itemTableModel.setColumnCount(0);
        itemTableModel.addColumn("Nummber");
        itemTableModel.addColumn("Kategorie");
        itemTableModel.addColumn("Beschreibung");
        itemTableModel.addColumn("Preis");
        itemTableModel.addColumn("ausgecheckt");
        for (Item item : items) {
            itemTableModel.addRow(new Object[]{
                    item.number,
                    (item.category != null) ? item.category.name : "",
                    item.description,
                    CURRENCY.format(item.price),
                    item.wasCheckedIn() ? "ja" : "nein"
            });
        }
        itemCodeField.requestFocus();
        pack();
    }

    public List<Item> getScannedItems() {
        return scannedItems;
    }

    private void onClose() {
        dispose();
    }

    private void onScan() {
        String itemCode = itemCodeField.getText();
        Item item = itemCodeMap.get(itemCode);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Der Artikel zu diesem Artikelcode ist nicht in der Liste.",
                    "Unbekannter Code", JOptionPane.ERROR_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }
        int index = items.indexOf(item);
        itemTable.setRowSelectionInterval(index, index);
        Rectangle rect = itemTable.getCellRect(index, 0, true);
        itemTable.scrollRectToVisible(rect);

        if (item.wasCheckedOut()) {
            JOptionPane.showMessageDialog(this, "Der Artikel ist bereits ausgecheckt.",
                    "Bereits ausgecheckt", JOptionPane.WARNING_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }

        if (scannedItems.contains(item)) {
            JOptionPane.showMessageDialog(this, "Der Artikel ist bereits eingescannt.",
                    "Bereits eingescannt", JOptionPane.WARNING_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }

        scannedItems.add(item);
        DefaultTableModel itemTableModel = (DefaultTableModel) itemTable.getModel();
        itemTableModel.setValueAt("GESCANNT", index, 4);

        checkOutButton.setText(String.format("%d Artikel auschecken", scannedItems.size()));

        itemCodeField.setText("");
        itemCodeField.requestFocus();
    }
}
