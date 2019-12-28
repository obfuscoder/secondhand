package de.obfusco.secondhand.gui.checkout;

import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
public class CheckoutGui extends JDialog {
    private JPanel contentPane;
    private JButton closeButton;
    private JTable reservationTable;
    private JTable itemTable;
    private JSplitPane splitPane;
    private JTextField itemCodeField;
    private JButton checkOutButton;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ItemRepository itemRepository;

    private ReservationTableModel reservationTableModel;
    private ItemTableModel itemTableModel;

    public CheckoutGui() {
        setContentPane(contentPane);
        setModal(true);

        checkOutButton.addActionListener(e -> onCheckOut());
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

        splitPane.setDividerLocation(400);
        itemCodeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    onCheckOut();
                }
            }
        });
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            reservationTableModel = new ReservationTableModel(reservationRepository.findAllByOrderByNumberAsc(), itemRepository);
            reservationTable.setModel(reservationTableModel);

            reservationTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
                Reservation reservation = reservationTableModel.getAt(reservationTable.getSelectedRow());
                if (reservation != null) {
                    itemTableModel = new ItemTableModel(itemRepository.findByReservationAndSoldNullAndCheckedInTrueOrderByNumberAsc(reservation));
                    itemTable.setModel(itemTableModel);
                } else
                    itemTable.setModel(new DefaultTableModel());
            });
            itemCodeField.requestFocus();
        }
        super.setVisible(b);
    }

    private void onCheckOut() {
        if (reservationTable.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie zuerst den Verkäufer.",
                    "Verkäufer wählen", JOptionPane.ERROR_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }
        String itemCode = itemCodeField.getText();
        Item item = itemRepository.findByCode(itemCode);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Dieser Artikelcode ist nicht bekannt. Bitte überprüfen Sie die Eingabe.",
                    "Unbekannter Code", JOptionPane.ERROR_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }
        if (!item.reservation.getId().equals(reservationTableModel.getAt(reservationTable.getSelectedRow()).getId())) {
            JOptionPane.showMessageDialog(this, "Achtung! Dieser Artikel gehört zu einem anderen Verkäufer!",
                    "Falscher Verkäufer", JOptionPane.WARNING_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }
        if (!item.checkedIn) {
            JOptionPane.showMessageDialog(this, "Dieser Artikel war gar nicht eingecheckt!",
                    "Nicht eingecheckt", JOptionPane.ERROR_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }
        if (item.wasSold()) {
            JOptionPane.showMessageDialog(this, "Dieser Artikel wurde verkauft!",
                    "Verkauft", JOptionPane.ERROR_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }

        if (item.checkedOut) {
            JOptionPane.showMessageDialog(this, "Dieser Artikel ist bereits ausgecheckt.",
                    "Bereits ausgecheckt", JOptionPane.WARNING_MESSAGE);
            itemCodeField.requestFocus();
            return;
        } else {
            checkOut(item);
        }

        updateCurrentReservation();
        selectAndScrollToItem(item);

        itemCodeField.setText("");
        itemCodeField.requestFocus();
    }

    private void checkOut(Item item) {
        item.checkedOut = true;
        itemRepository. save(item);
    }

    private void selectAndScrollToItem(Item item) {
        int itemIndex = itemTableModel.update(item);
        itemTable.setRowSelectionInterval(itemIndex, itemIndex);
        Rectangle rect = itemTable.getCellRect(itemIndex, 0, true);
        itemTable.scrollRectToVisible(rect);
    }

    private void updateCurrentReservation() {
        reservationTableModel.fireTableRowsUpdated(reservationTable.getSelectedRow(), reservationTable.getSelectedRow());
    }

    private void onClose() {
        dispose();
    }
}
