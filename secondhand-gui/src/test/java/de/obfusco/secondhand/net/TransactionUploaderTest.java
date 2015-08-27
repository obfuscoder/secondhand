package de.obfusco.secondhand.net;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.obfusco.secondhand.gui.MainConfiguration;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.repository.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MainConfiguration.class)
public class TransactionUploaderTest {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    TransactionUploader transactionUploader;

    @Test
    public void uploadTransactions() throws IOException {
        createTransactions();
        assertTrue(transactionUploader.upload("localhost:3000", "MqQVsBFos1E"));
    }

    private void createTransactions() {
        transactionRepository.deleteAll();
        Iterable<Item> allItems = itemRepository.findAll();
        List<Item> items = new ArrayList<>();
        for (Item item : allItems) {
            items.add(item);
        }
        transactionRepository.save(Transaction.create(Transaction.Type.PURCHASE, items.subList(0, 2), "12345"));
        transactionRepository.save(Transaction.create(Transaction.Type.PURCHASE, items.subList(2, 3), null));
        transactionRepository.save(Transaction.create(Transaction.Type.REFUND, items.subList(0, 1), null));
    }
}