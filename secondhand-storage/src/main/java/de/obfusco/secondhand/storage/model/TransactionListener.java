package de.obfusco.secondhand.storage.model;

public interface TransactionListener {
    void notify(Transaction transaction);

    void transactionReceived(Transaction transaction);
}
