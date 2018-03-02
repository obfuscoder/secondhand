package de.obfusco.secondhand.refund.gui;

import de.obfusco.secondhand.storage.model.BaseItem;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.repository.ItemRepository;
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
public class RefundGui extends JFrame implements ActionListener, TableModelListener {

    private NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private JTextField itemNr;
    private ItemTableModel tableModel;
    private JLabel errorLabel;
    private JLabel priceLabel;
    private JTable itemTable;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    StorageService storageService;

    @Autowired
    TransactionListener transactionListener;

    private JButton readyButton = new JButton("Fertig");

    private CommitRefundDialog checkout = null;
    private JLabel countLabel;

    public RefundGui() {
        super("Storno");
        setSize(1000, 700);
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

    public String getPrice() {
        return priceLabel.getText();
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
        }
    }

    private void newCustomer() {
        checkout = null;
        readyButton.setEnabled(false);
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

        checkout = new CommitRefundDialog(this, transactionListener);
        checkout.setLocationRelativeTo(this);
        checkout.setVisible(true);
        newCustomer();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        calcTotalPriceAndCount();
    }

    private void calcTotalPriceAndCount() {
        int rowCount = tableModel.getRowCount();
        readyButton.setEnabled(rowCount > 0);
        double totalPrice = 0;
        for (int i = 0; i < rowCount; i++) {
            BigDecimal price = tableModel.getData().get(i).price;
            totalPrice += price.doubleValue();
        }
        priceLabel.setText(String.format("%.2f", totalPrice).replace('.', ','));
        countLabel.setText(Integer.toString(rowCount));
    }

    List<BaseItem> getTableData() {
        return tableModel.getData();
    }

    public StorageService getStorageService() {
        return storageService;
    }

    private void addItem() {
        String code = itemNr.getText();
        itemNr.setText("");
        setErrorText("");
        BaseItem item = storageService.getItem(code);
        if (item == null) {
            setErrorText("Artikel mit Nummer \"" + code + "\" existiert nicht!");
            return;
        }
        if (!item.canRefund()) {
            setErrorText("Artikel mit Nummer \"" + code + "\" wurde noch nicht verkauft!");
            return;
        }
        tableModel.addRow(item);
    }

    private void setErrorText(String text) {
        errorLabel.setText(text);
        errorLabel.getParent().invalidate();
        errorLabel.getParent().validate();
        this.validate();
    }

    private void deleteSelectedRow() {
        tableModel.delRow(itemTable.getSelectedRow());
        itemNr.requestFocus();
    }

    class ItemTableModel extends AbstractTableModel {

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
                    return currency.format(item.price);
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
