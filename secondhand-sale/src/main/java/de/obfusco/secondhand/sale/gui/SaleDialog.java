package de.obfusco.secondhand.sale.gui;

import de.obfusco.secondhand.storage.model.BaseItem;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class SaleDialog extends JFrame implements ActionListener, TableModelListener {

    private static NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private JTextField itemNr;
    private CashTableModel tableModel;
    private JLabel errorLabel;
    private JLabel priceLabel;
    private JTable cashTable;
    BigDecimal totalPrice = BigDecimal.ZERO;

    @Autowired
    StorageService storageService;

    @Autowired
    TransactionListener transactionListener;

    @Autowired
    EventRepository eventRepository;

    private JButton readyButton = new JButton("Fertig");

    private FinalizeSaleDialog finalizeSaleDialog = null;
    private JLabel countLabel;

    public SaleDialog() {
        super("Flohmarkt Verkauf");
        setSize(1000, 700);
        addComponentsToPane(getContentPane());
        setLocationRelativeTo(null);
    }

    private void addComponentsToPane(Container pane) {
        pane.setFont(pane.getFont().deriveFont(20f));

        JLabel title = new JLabel("Flohmarkt Verkauf");
        title.setFont(title.getFont().deriveFont(40.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        errorLabel = new JLabel("");
        errorLabel.setForeground(new Color(255, 0, 0, 255));
        errorLabel.setFont(errorLabel.getFont().deriveFont(30f));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        tableModel = new CashTableModel();

        cashTable = new JTable(tableModel);
        cashTable.setFont(pane.getFont());
        cashTable.setRowHeight(30);
        cashTable.addKeyListener(new KeyAdapter() {
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
        cashTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        cashTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        cashTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        cashTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        itemNr = new JTextField();
        itemNr.setFont(itemNr.getFont().deriveFont(20f));
        itemNr.setHorizontalAlignment(SwingConstants.CENTER);
        itemNr.addKeyListener(new KeyAdapter() {

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
        JPanel topPanel = new JPanel(new BorderLayout());
        itemPanel.add(topPanel, BorderLayout.NORTH);
        topPanel.add(itemNr, BorderLayout.NORTH);
        topPanel.add(errorLabel, BorderLayout.SOUTH);
        itemPanel.add(new JScrollPane(cashTable), BorderLayout.CENTER);

        JLabel countDescLabel = new JLabel("Artikel: ");
        countLabel = new JLabel("0");
        JLabel sumLabel = new JLabel("Summe: ");
        priceLabel = new JLabel(CURRENCY.format(totalPrice));
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
        countPanel.add(countDescLabel);
        countPanel.add(countLabel);
        sumPanel.add(sumLabel);
        sumPanel.add(priceLabel);
        pane.add(itemPanel, BorderLayout.CENTER);

        readyButton.setEnabled(false);
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        readyButton.setFont(pane.getFont().deriveFont(30f));

        buttonPanel.add(readyButton);

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

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(2, 0));
        southPanel.add(totalPanel);
        southPanel.add(buttonPanel);

        pane.add(southPanel, BorderLayout.SOUTH);
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public JTextField getItemNr() {
        return itemNr;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == readyButton) {
            openDialog();
        }
    }

    public void newCustomer() {
        finalizeSaleDialog = null;
        readyButton.setEnabled(false);
        itemNr.setEnabled(true);
        cashTable.setEnabled(true);

        itemNr.setText("");
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            tableModel.delRow(0);
        }

        validate();

        itemNr.requestFocus();
    }

    private void openDialog() {
        errorLabel.setText("");

        finalizeSaleDialog = new FinalizeSaleDialog(this, transactionListener);
        finalizeSaleDialog.setLocationRelativeTo(this);
        finalizeSaleDialog.setVisible(true);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        calcTotalPriceAndCount();
    }

    private void calcTotalPriceAndCount() {
        int rowCount = tableModel.getRowCount();
        readyButton.setEnabled(rowCount > 0);
        totalPrice = BigDecimal.ZERO;
        for (int i = 0; i < rowCount; i++) {
            totalPrice = totalPrice.add(tableModel.getData().get(i).price);
        }
        priceLabel.setText(CURRENCY.format(totalPrice));
        countLabel.setText(Integer.toString(rowCount));
    }

    List<BaseItem> getTableData() {
        return tableModel.getData();
    }

    private void addItem() {
        String code = itemNr.getText();
        setErrorText("");
        BaseItem item = storageService.getItem(code);
        if (item == null) {
            setErrorText("Artikel existiert nicht!");
            return;
        }
        if (!storageService.canBeSold(item)) {
            setErrorText("Artikel kann nicht verkauft werden!");
            return;
        }
        itemNr.setText("");
        tableModel.addRow(item);
    }

    private void setErrorText(String text) {
        errorLabel.setText(text);
        errorLabel.getParent().invalidate();
        errorLabel.getParent().validate();
        this.validate();
    }

    private void deleteSelectedRow() {
        int n = JOptionPane.showConfirmDialog(
                this,
                "Möchten sie den Artikel \""
                        + tableModel.getValueAt(cashTable.getSelectedRow(), 0)
                        + "\" wirklich löschen?", "Artikel löschen",
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
            tableModel.delRow(cashTable.getSelectedRow());
        }

        itemNr.requestFocus();

    }

    class CashTableModel extends AbstractTableModel {

        private List<String> columnNames = new ArrayList<>(Arrays.asList(
                "ArtNr", "Kategorie", "Bezeichnung", "Groesse", "Preis"));

        private List<BaseItem> data = new ArrayList<>();

        List<BaseItem> getData() {
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
            BaseItem item = data.get(row);
            switch (col) {
                case 0:
                    return item.code;
                case 1:
                    return item.getCategoryName();
                case 2:
                    return item.description;
                case 3:
                    return item.getSize();
                case 4:
                    return CURRENCY.format(item.price);
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int index) {
            return columnNames.get(index);
        }

        Boolean findItemNr(String nr) {

            for (BaseItem item : data) {
                if (item.code.equals(nr)) {
                    return true;
                }
            }
            return false;
        }

        void addRow(BaseItem item) {
            if (item.isUnique() && findItemNr(item.code)) {
                setErrorText("Artikel schon vorhanden!");
            } else {
                data.add(item);
                this.fireTableDataChanged();
            }
        }

        void delRow(int row) {
            data.remove(row);
            this.fireTableDataChanged();
        }
    }
}
