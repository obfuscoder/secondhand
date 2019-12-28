package de.obfusco.secondhand.gui.checkout;

import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.ItemRepository;

import javax.swing.table.AbstractTableModel;
import java.util.List;

class ReservationTableModel extends AbstractTableModel {
    private List<Reservation> reservations;

    ItemRepository itemRepository;

    public ReservationTableModel(List<Reservation> reservations, ItemRepository itemRepository) {
        this.reservations = reservations;
        this.itemRepository = itemRepository;
    }

    @Override
    public int getRowCount() {
        return reservations.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Nummer";
            case 1:
                return "Name";
            case 2:
                return "eingecheckt";
            case 3:
                return "ausgecheckt";
        }
        return super.getColumnName(column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        Reservation reservation = reservations.get(row);
        switch (column) {
            case 0:
                return reservation.number;
            case 1:
                return reservation.seller.getName();
            case 2:
                return itemRepository.countByReservationAndCheckedInTrue(reservation);
            case 3:
                return itemRepository.countByReservationAndCheckedOutTrue(reservation);
        }
        return null;
    }

    public Reservation getAt(int row) {
        if (row >= 0)
            return reservations.get(row);
        else
            return null;
    }

    public int update(Reservation reservation) {
        for(int i=0; i<reservations.size(); i++)
            if (reservations.get(i).getId().equals(reservation.getId())) {
                fireTableRowsUpdated(i, i);
                return i;
            }
        return -1;
    }
}
