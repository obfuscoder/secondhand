package de.obfusco.secondhand.refund.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagLayout;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefundGui extends JFrame implements ActionListener, TableModelListener {

    protected NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    JTextField itemNr;
    ItemTableModel tableModel;
    JLabel errorLabel;
    JLabel priceLabel;
    JTable itemTable;
    String sum;

    @Autowired
    ReservedItemRepository itemRepository;

    JButton readyButton = new JButton("Fertig");
    JButton newButton = new JButton("Neue Rückgabe");

    CheckOutDialog checkout = null;
    private JLabel countLabel;

    public RefundGui() {
        super("Storno");
        setSize(1000, 800);
        addComponentsToPane(getContentPane());
        setLocationRelativeTo(null);
    }

    private void addComponentsToPane(Container pane) {
        pane.setFont(pane.getFont().deriveFont(20f));

        JLabel title = new JLabel("Flohmarkt Storno");
        title.setFont(title.getFont().deriveFont(40.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        errorLabel = new JLabel("");
        errorLabel.setForeground(new Color(255, 0, 0, 255));
        errorLabel.setFont(errorLabel.getFont().deriveFont(30f));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        tableModel = new ItemTableModel();

        itemTable = new JTable(tableModel);
        itemTable.setFont(pane.getFont());
        itemTable.setRowHeight(30);
        itemTable.addKeyListener(new KeyListener() {

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
        tableModel.addTableModelListener(this);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        itemTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        itemTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        itemTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        itemNr = new JTextField();
        itemNr.setFont(itemNr.getFont().deriveFont(20f));
        itemNr.setHorizontalAlignment(SwingConstants.CENTER);
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
                            && itemTable.getModel().getRowCount() > 0) {
                        itemNr.setText("");
                        openDialog();
                        return;
                    }
                    if (itemText.length() != 8 || !itemText.matches("\\d{8}")) {
                        setErrorText("Artikelnummer " + itemNr.getText() + " ist falsch.");
                        itemNr.setText("");
                        return;
                    }

                    if (tableModel.findItemNr(itemNr.getText())) {
                        setErrorText("Artikelnummer " + itemNr.getText()
                                + " bereits eingescannt!");
                        itemNr.setText("");
                        return;
                    }

                    addItem();
                }

            }
        });

        itemTable.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                itemTable.scrollRectToVisible(itemTable.getCellRect(itemTable.getRowCount() - 1, 0, true));
            }
        });

        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        itemPanel.add(topPanel, BorderLayout.NORTH);
        topPanel.add(itemNr, BorderLayout.NORTH);
        topPanel.add(errorLabel, BorderLayout.SOUTH);
        itemPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        JLabel countDescLabel = new JLabel("Artikel: ");
        countLabel = new JLabel("0");
        JLabel sumLabel = new JLabel("Summe: ");
        priceLabel = new JLabel("0,00");
        JLabel euroLabel = new JLabel(" €");
        JPanel totalPanel = new JPanel(new GridLayout(0, 2));
        JPanel countPanel = new JPanel(new GridBagLayout());
        JPanel sumPanel = new JPanel(new GridBagLayout());
        totalPanel.add(countPanel);
        totalPanel.add(sumPanel);
        countDescLabel.setFont(pane.getFont().deriveFont(40f));
        countLabel.setFont(pane.getFont().deriveFont(40f));
        sumLabel.setFont(pane.getFont().deriveFont(40f));
        priceLabel.setFont(pane.getFont().deriveFont(40f));
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        euroLabel.setFont(pane.getFont().deriveFont(40f));
        countPanel.add(countDescLabel);
        countPanel.add(countLabel);
        sumPanel.add(sumLabel);
        sumPanel.add(priceLabel);
        sumPanel.add(euroLabel);
        pane.add(itemPanel, BorderLayout.CENTER);

        newButton.setEnabled(false);
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        readyButton.setFont(pane.getFont().deriveFont(30f));
        newButton.setFont(pane.getFont().deriveFont(30f));

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
        southPanel.add(totalPanel);
        southPanel.add(buttonPanel);

        pane.add(southPanel, BorderLayout.SOUTH);
    }

    public ReservedItemRepository getItemRepository() {
        return itemRepository;
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
        return itemTable;
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
        itemTable.setEnabled(true);

        itemNr.setText("");
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            tableModel.delRow(0);
        }

        priceLabel.setText("0,00");
        countLabel.setText("0");
        validate();

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
        int rowCount = tableModel.getRowCount();
        double totalPrice = 0;
        for (int i = 0; i < rowCount; i++) {
            BigDecimal price = tableModel.getData().get(i).getItem().getPrice();
            totalPrice += price.doubleValue();
        }
        priceLabel.setText(String.format("%.2f", totalPrice).replace('.', ','));
        countLabel.setText(Integer.toString(rowCount));
    }

    List<ReservedItem> getTableData() {
        return tableModel.getData();
    }

    private static class App {

        public App() {
        }
    }

    class ItemTableModel extends AbstractTableModel {

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
        setErrorText("");
        ReservedItem reservedItem = itemRepository.findByCode(code);
        if (reservedItem == null) {
            setErrorText("Artikel mit Nummer \"" + code + "\" existiert nicht!");
            return;
        }
        if (!reservedItem.isSold()) {
            setErrorText("Artikel mit Nummer \"" + code
                    + "\" wurde noch nicht verkauft!");
            return;
        }
        if (reservedItem.isRefunded()) {
            setErrorText("Artikel mit Nummer \"" + code
                    + "\" wurde bereits storniert!");
            return;
        }
        tableModel.addRow(reservedItem);
    }

    public void setErrorText(String text) {
        errorLabel.setText(text);
        errorLabel.getParent().invalidate();
        errorLabel.getParent().validate();
        this.validate();
    }

    public void deleteSelectedRow() {
        tableModel.delRow(itemTable.getSelectedRow());
        itemNr.requestFocus();
    }
}