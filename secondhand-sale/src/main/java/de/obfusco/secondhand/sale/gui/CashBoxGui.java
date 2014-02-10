package de.obfusco.secondhand.sale.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.obfusco.secondhand.sale.service.StorageService;
import de.obfusco.secondhand.storage.model.ReservedItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashBoxGui extends JFrame implements ActionListener, TableModelListener {

    private static final long serialVersionUID = -698049510249510666L;

    protected NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    JTextField itemNr;
    CashTableModel tablemodel;
    JLabel errorLabel;
    JLabel priceLabel;
    JTable cashTable;
    String sum;

    @Autowired
    StorageService storageService;

    JButton readyButton = new JButton("Fertig");
    JButton newButton = new JButton("Neuer Kunde");

    CheckOutDialog checkout = null;
    private JLabel countLabel;

    public CashBoxGui() {
        super("Flohmarkt Verkauf");
        setSize(800, 600);
        addComponentsToPane(getContentPane());
        pack();
        setLocationRelativeTo(null);
    }

    private void addComponentsToPane(Container pane) {

        JLabel title = new JLabel("Flohmarkt Verkauf");
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
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedRow();
                }
            }
        });
        tablemodel.addTableModelListener(this);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        cashTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        cashTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        cashTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        cashTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        itemNr = new JTextField();
        itemNr.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String itemText = itemNr.getText();
                    if (itemText.length() == 0
                            && cashTable.getModel().getRowCount() > 0) {
                        itemNr.setText("");
                        openDialog();
                        return;
                    }
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

        cashTable.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                cashTable.scrollRectToVisible(cashTable.getCellRect(cashTable.getRowCount() - 1, 0, true));
            }
        });

        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.add(itemNr, BorderLayout.NORTH);
        itemPanel.add(new JScrollPane(cashTable), BorderLayout.CENTER);
        itemPanel.add(errorLabel, BorderLayout.SOUTH);

        JLabel countDescLabel = new JLabel("Artikel: ");
        countLabel = new JLabel("0");
        JLabel sumLabel = new JLabel("Summe: ");
        priceLabel = new JLabel(currency.format(0));
        JLabel euroLabel = new JLabel("Euro");
        JPanel sumPanel = new JPanel(new GridLayout(0, 5));
        countDescLabel.setFont(countDescLabel.getFont().deriveFont(20.0f));
        countLabel.setFont(countLabel.getFont().deriveFont(20.0f));
        sumLabel.setFont(title.getFont().deriveFont(20.0f));
        priceLabel.setFont(title.getFont().deriveFont(20.0f));
        euroLabel.setFont(euroLabel.getFont().deriveFont(20.0f));
        sumPanel.add(countDescLabel);
        sumPanel.add(countLabel);
        sumPanel.add(sumLabel);
        sumPanel.add(priceLabel);
        sumPanel.add(euroLabel);
        pane.add(itemPanel, BorderLayout.CENTER);

        newButton.setEnabled(false);
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2));

        buttonPanel.add(readyButton);
        buttonPanel.add(newButton);

        readyButton.addActionListener(this);
        readyButton.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openDialog();
                }
            }
        });
        newButton.addActionListener(this);
        newButton.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    newCustomer();
                }

            }
        });

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(2, 0));
        southPanel.add(sumPanel);
        southPanel.add(buttonPanel);

        pane.add(southPanel, BorderLayout.SOUTH);
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public String getPrice() {
        return priceLabel.getText();
    }

    public JButton getNewButton() {
        return newButton;
    }

    public JButton getReadyButton() {
        return readyButton;
    }

    public JTextField getItemNr() {
        return itemNr;
    }

    public JTable getCashTable() {
        return cashTable;
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == readyButton) {

            openDialog();

        } else if (event.getSource() == newButton) {

            newCustomer();
        }
    }

    private void newCustomer() {
        checkout = null;
        newButton.setEnabled(false);
        readyButton.setEnabled(true);
        itemNr.setEnabled(true);
        cashTable.setEnabled(true);

        itemNr.setText("");
        int rowCount = tablemodel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            tablemodel.delRow(0);
        }

        priceLabel.setText(currency.format(0));
        countLabel.setText("0");
        validate();
        pack();

        itemNr.requestFocus();
    }

    private void openDialog() {
        errorLabel.setText("");

        checkout = new CheckOutDialog(this);
        checkout.setLocationRelativeTo(this);
        checkout.setVisible(true);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        calcTotalPriceAndCount();
    }

    private void calcTotalPriceAndCount() {
        int rowCount = tablemodel.getRowCount();
        double totalPrice = 0;
        for (int i = 0; i < rowCount; i++) {
            BigDecimal price = tablemodel.getData().get(i).getItem().getPrice();
            totalPrice += price.doubleValue();
        }
        priceLabel.setText(String.format("%.2f", totalPrice).replace('.', ','));
        countLabel.setText(Integer.toString(rowCount));
    }

    List<ReservedItem> getTableData() {
        return tablemodel.getData();
    }

    private static class App {

        public App() {
        }
    }

    class CashTableModel extends AbstractTableModel {

        private List<String> columnNames = new ArrayList<>(Arrays.asList(
                "ArtNr", "Kategorie", "Bezeichnung", "Groesse", "Preis"));

        private List<ReservedItem> data = new ArrayList<>();

        public List<ReservedItem> getData() {
            return data;
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
            ReservedItem item = data.get(row);
            switch (col) {
                case 0:
                    return item.getCode();
                case 1:
                    return item.getItem().getCategory().getName();
                case 2:
                    return item.getItem().getDescription();
                case 3:
                    return item.getItem().getSize();
                case 4:
                    return currency.format(item.getItem().getPrice());
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int index) {
            return columnNames.get(index);
        }

        public Boolean findItemNr(String nr) {

            for (ReservedItem item : data) {
                if (item.getCode().equals(nr)) {
                    return true;
                }
            }
            return false;
        }

        public void addRow(ReservedItem item) {
            if (findItemNr(item.getCode())) {
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
        String code = itemNr.getText();
        itemNr.setText("");
        setErrorText(" ");
        ReservedItem reservedItem = storageService.getReservedItem(code);
        if (reservedItem == null) {
            setErrorText("Artikel mit Nummer \"" + code + "\" existiert nicht!");
            return;
        }
        if (reservedItem.isSold()) {
            setErrorText("Artikel mit Nummer \"" + code
                    + "\" wurde bereits verkauft!");
            return;
        }
        tablemodel.addRow(reservedItem);
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
