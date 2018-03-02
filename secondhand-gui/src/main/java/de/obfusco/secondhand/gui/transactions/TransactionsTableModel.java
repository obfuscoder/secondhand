package de.obfusco.secondhand.gui.transactions;

import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

@Component
class TransactionsTableModel extends AbstractTableModel {
    @Autowired
    TransactionRepository transactionRepository;

    private List<Transaction> transactions;

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0: return "Zeit";
            case 1: return "Typ";
            case 2: return "Artikelzahl";
        }
        return null;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Transaction transaction = transactions.get(row);
        switch(column) {
            case 0: return transaction.created;
            case 1: return transaction.type;
            case 2: return transaction.getAllItems().size();
        }
        return null;
    }

    public void init() {
        transactions = new ArrayList<>();
        for (Transaction transaction : transactionRepository.findAll()) {
            transactions.add(transaction);
        }
    }

    public Transaction getAt(int row) {
        return transactions.get(row);
    }
}
