package de.obfusco.secondhand.testscan.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestScanGui extends JFrame implements ActionListener {

    private static final long serialVersionUID = -698049510249510666L;
    JTextField itemNr;
    CashTableModel tablemodel;
    JLabel errorLabel;
    JTable cashTable;

    JButton clearButton = new JButton("Tabelle leeren");

    @Autowired
    private ReservedItemRepository reservedItemRepository;

    public TestScanGui() {
        super("Barcode Test");
        setSize(600, 600);
        addComponentsToPane(getContentPane());
        pack();
        setLocationRelativeTo(null);
    }

    private void addComponentsToPane(Container pane) {

        JLabel title = new JLabel("Barcode Test");
        title.setFont(title.getFont().deriveFont(24.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 0, 0, 255));

        tablemodel = new CashTableModel();

        cashTable = new JTable(tablemodel);
        cashTable.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedRow();
                }

            }
        });

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        cashTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        itemNr = new JTextField();
        itemNr.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String itemText = itemNr.getText();
                    if (itemText.length() != 8) {
                        setErrorText("Artikelnummer "
                                + itemNr.getText()
                                + " ist falsch! Die Nummer muss 8 Zeichen lang sein!");

                        itemNr.setText("");
                        return;
                    }

                    if (tablemodel.findItemNr(itemNr.getText())) {
                        setErrorText("Artikelnummer " + itemNr.getText()
                                + " bereits eingescannt!");
                        itemNr.setText("");
                        return;
                    }

                    addItem();
                }

            }
        });

        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.add(itemNr, BorderLayout.NORTH);
        itemPanel.add(new JScrollPane(cashTable), BorderLayout.CENTER);
        // itemPanel.add(removeButton, BorderLayout.EAST);
        itemPanel.add(errorLabel, BorderLayout.SOUTH);

        pane.add(itemPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 3));

        buttonPanel.add(clearButton);

        clearButton.addActionListener(this);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout());
        southPanel.add(buttonPanel);

        pane.add(southPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == clearButton) {

            itemNr.setText("");
            int rowCount = tablemodel.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                tablemodel.delRow(0);
            }
            itemNr.requestFocus();
        }

    }

    class CashTableModel extends AbstractTableModel {

        private List<String> columnNames = new ArrayList<String>(Arrays.asList(
                "ArtNr", "Kategorie", "Bezeichnung", "Groesse", "Preis"));

        private List<ReservedItem> data = new ArrayList<>();

        public List<ReservedItem> getData() {
            return data;
        }

        public List<Object> getColumnData(int col) {
            List<Object> columnData = new ArrayList<Object>();
            for (int i = 0; i < data.size(); i++) {
                ReservedItem item = data.get(i);
                String columnValue = getItemValueForColumn(item, col);
                columnData.add(columnValue);
            }

            return columnData;
        }

        private String getItemValueForColumn(ReservedItem item, int col) {
            String columnValue = "";
            switch (col) {
                case 0:
                    columnValue = item.getCode();
                    break;
                case 1:
                    columnValue = item.getItem().getCategory().getName();
                    break;
                case 2:
                    columnValue = item.getItem().getDescription();
                    break;
                case 3:
                    columnValue = item.getItem().getSize();
                    break;
                case 4:
                    columnValue = item.getItem().getPrice().toString();
                    break;
            }
            return columnValue;
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return getItemValueForColumn(data.get(row), col);
        }

        @Override
        public String getColumnName(int index) {
            return (String) columnNames.get(index);
        }

        public Boolean findItemNr(String nr) {

            for (int i = 0; i < getRowCount(); i++) {
                // System.out.println("Value at " + i + " 0 = \"" +
                // getValueAt(i, 0) + "\"" + " == \"" + nr + "\"");
                if (getValueAt(i, 0).equals(nr)) {
                    return true;
                }
            }
            return false;
        }

        public void addRow(ReservedItem item) {

            if (data.contains(item)) {
                setErrorText("Artikel schon vorhanden!");
            } else {
                data.add(item);
                this.fireTableDataChanged();
            }
        }

        public void delRow(int row) {
            data.remove(row);
            this.fireTableDataChanged();
        }
    }

    public void addItem() {
        setErrorText(" ");
        ReservedItem item = reservedItemRepository.findByCode(itemNr.getText());
        if (item != null) {
            tablemodel.addRow(item);
        } else {
            setErrorText("Artikel mit Nummer \"" + itemNr.getText()
                    + "\" existiert nicht!");
        }

        itemNr.setText("");
    }

    public void setErrorText(String text) {
        errorLabel.setText(text);
        errorLabel.getParent().invalidate();
        errorLabel.getParent().validate();
        this.validate();
        this.pack();
    }

    public void deleteSelectedRow() {
        int n = JOptionPane.showConfirmDialog(
                this,
                "Möchten sie den Artikel \""
                + tablemodel.getValueAt(cashTable.getSelectedRow(), 0)
                + "\" wirklich löschen?", "Artikel löschen",
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
            tablemodel.delRow(cashTable.getSelectedRow());
        }

        itemNr.requestFocus();

    }
}
