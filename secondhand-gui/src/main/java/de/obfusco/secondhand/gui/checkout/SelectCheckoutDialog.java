package de.obfusco.secondhand.gui.checkout;

import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import de.obfusco.secondhand.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
            reservationTableModel.addColumn("auszucheckende Artikel");
            reservationTableModel.addColumn("ausgecheckte Artikel");

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
}
