package de.obfusco.secondhand.gui.checkout;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import de.obfusco.secondhand.storage.service.StorageService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class SelectCheckoutDialog extends JDialog {
    private JPanel contentPane;
    private JButton closeButton;
    private JTable reservationTable;

    private List<Reservation> reservations;

    private ReservationRepository reservationRepository;
    private ItemRepository itemRepository;
    private StorageService storageService;
    private TransactionListener transactionListener;

    public SelectCheckoutDialog(ReservationRepository reservationRepository, ItemRepository itemRepository, StorageService storageService, TransactionListener transactionListener) {
        this.reservationRepository = reservationRepository;
        this.itemRepository = itemRepository;
        this.storageService = storageService;
        this.transactionListener = transactionListener;

        setContentPane(contentPane);
        setModal(true);

        closeButton.addActionListener(e -> onClose());

        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            reservationTable.clearSelection();
            DefaultTableModel reservationTableModel = (DefaultTableModel) reservationTable.getModel();
            reservationTableModel.setRowCount(0);
            reservationTableModel.setColumnCount(0);
            reservationTableModel.addColumn("Nummer");
            reservationTableModel.addColumn("Name");
            reservationTableModel.addColumn("eingecheckt");
            reservationTableModel.addColumn("ausgecheckt");

            reservationTable.setDefaultRenderer(Object.class, new TableCellRender());

            reservations = reservationRepository.findAllByOrderByNumberAsc();
            for (Reservation reservation : reservations) {
                reservationTableModel.addRow(new Object[]{
                        reservation.number,
                        reservation.seller.getName(),
                        itemRepository.countByReservationAndCheckedInNotNullAndSoldNullAndDonationFalse(reservation),
                        itemRepository.countByReservationAndCheckedOutNotNull(reservation)
                });
            }

            reservationTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
                int row = reservationTable.getSelectedRow();
                if (row < 0) return;
                Reservation reservation = reservations.get(reservationTable.getSelectedRow());
                if (reservation != null) {
                    List<Item> items = itemRepository.findByReservationAndSoldNullAndCheckedInNotNullAndDonationFalseOrderByNumberAsc(reservation);
                    CheckoutDialog checkoutDialog = new CheckoutDialog(items);
                    checkoutDialog.setLocationRelativeTo(this);
                    checkoutDialog.setVisible(true);
                    List<Item> scannedItems = checkoutDialog.getScannedItems();
                    if (scannedItems.size() == 0) return;

                    Transaction transaction = storageService.checkOutItems(scannedItems);
                    transactionListener.notify(transaction);
                    long newCount = itemRepository.countByReservationAndCheckedOutNotNull(reservation);
                    reservationTableModel.setValueAt(newCount, row, 3);
                }
            });
        }
        super.setVisible(b);
    }

    private void onClose() {
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Schließen");
        panel1.add(closeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(500, 400), null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Reservierungen"));
        reservationTable = new JTable();
        scrollPane1.setViewportView(reservationTable);
        final JLabel label1 = new JLabel();
        label1.setText("Bitte die Reservierung anklicken, für die Artikel ausgecheckt werden sollen.");
        contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private static class TableCellRender extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            int checkedOutCount = Integer.parseInt(table.getValueAt(row, 2).toString());
            int checkedInCount = Integer.parseInt(table.getValueAt(row, 3).toString());
            Color fontColor = (checkedOutCount == 0) ? Color.GRAY : Color.BLACK;
            Color backgroundColor = (checkedOutCount > 0 && checkedInCount == checkedOutCount) ? Color.GREEN : Color.WHITE;
            component.setForeground(fontColor);
            component.setBackground(backgroundColor);
            return component;
        }
    }
}