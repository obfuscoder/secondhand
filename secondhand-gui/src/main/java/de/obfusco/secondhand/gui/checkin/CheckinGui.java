package de.obfusco.secondhand.gui.checkin;

import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

@Component
public class CheckinGui extends JDialog {
    private JPanel contentPane;
    private JButton closeButton;
    private JTable reservationTable;
    private JTable itemTable;
    private JSplitPane splitPane;
    private JTextField itemCodeField;
    private JButton checkInButton;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ItemRepository itemRepository;

    private ReservationTableModel reservationTableModel;
    private ItemTableModel itemTableModel;

    public CheckinGui() {
        setContentPane(contentPane);
        setModal(true);

        checkInButton.addActionListener(e -> onCheckIn());
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
                    onCheckIn();
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
                    itemTableModel = new ItemTableModel(itemRepository.findByReservationOrderByNumberAsc(reservation));
                    itemTable.setModel(itemTableModel);
                } else
                    itemTable.setModel(new DefaultTableModel());
            });
            itemCodeField.requestFocus();
        }
        super.setVisible(b);
    }

    private void onCheckIn() {
        String itemCode = itemCodeField.getText();
        Item item = itemRepository.findByCode(itemCode);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Dieser Artikelcode ist nicht bekannt. Bitte überprüfen Sie die Eingabe.",
                    "Unbekannter Code", JOptionPane.ERROR_MESSAGE);
            itemCodeField.requestFocus();
            return;
        }
        if (item.checkedIn) {
            JOptionPane.showMessageDialog(this, "Der Artikel ist bereits eingecheckt.",
                    "Bereits eingecheckt", JOptionPane.WARNING_MESSAGE);
            itemCodeField.requestFocus();
        } else {
            checkIn(item);
        }

        selectAndScrollToReservation(item.reservation);
        selectAndScrollToItem(item);

        itemCodeField.setText("");
        itemCodeField.requestFocus();
    }

    private void checkIn(Item item) {
        item.checkedIn = true;
        itemRepository.save(item);
    }

    private void selectAndScrollToItem(Item item) {
        int itemIndex = itemTableModel.update(item);
        itemTable.setRowSelectionInterval(itemIndex, itemIndex);
        Rectangle rect = itemTable.getCellRect(itemIndex, 0, true);
        itemTable.scrollRectToVisible(rect);
    }

    private void selectAndScrollToReservation(Reservation reservation) {
        int reservationIndex = reservationTableModel.update(reservation);
        reservationTable.setRowSelectionInterval(reservationIndex, reservationIndex);
        Rectangle rect = reservationTable.getCellRect(reservationIndex, 0, true);
        reservationTable.scrollRectToVisible(rect);
    }

    private void onClose() {
        dispose();
    }
}
