package de.obfusco.secondhand.reports;

import de.obfusco.secondhand.net.Network;
import de.obfusco.secondhand.net.Peer;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.StockItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import de.obfusco.secondhand.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class ReportsGui extends JFrame {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    StorageService storageService;

    private JLabel itemCount;
    private JLabel stockItemCount;
    private JLabel transactionCount;
    private JLabel soldItemCount;
    private JLabel soldStockItemCount;
    private JLabel soldSum;
    private JLabel connections;

    private Network network;

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    public ReportsGui() {
        super("Flohmarkt Reports");
        addComponentsToPane(getContentPane());
        setLocationRelativeTo(null);
    }

    private void addComponentsToPane(Container contentPane) {
        JPanel panel = new JPanel();
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        panel.setBorder(padding);
        GridLayout layout = new GridLayout(0,2, 10, 10);
        panel.setLayout(layout);
        panel.add(new JLabel("Anzahl Artikel:"));
        panel.add(itemCount = new JLabel());
        panel.add(new JLabel("Anzahl Stammartikel:"));
        panel.add(stockItemCount = new JLabel());
        panel.add(new JLabel("Anzahl Transaktionen:"));
        panel.add(transactionCount = new JLabel());
        panel.add(new JLabel("Anzahl verkaufter Artikel:"));
        panel.add(soldItemCount = new JLabel());
        panel.add(new JLabel("Anzahl verkaufter Stammartikel:"));
        panel.add(soldStockItemCount = new JLabel());
        panel.add(new JLabel("Umsatz:"));
        panel.add(soldSum = new JLabel());
        panel.add(new JLabel("Verbunden mit:"));
        panel.add(connections = new JLabel());
        contentPane.add(panel);
        pack();
    }

    public void update() {
        itemCount.setText("" + itemRepository.count());
        stockItemCount.setText("" + stockItemRepository.count());
        transactionCount.setText("" + transactionRepository.count());
        soldItemCount.setText("" + itemRepository.countBySoldNotNull());
        Long soldStockItems = stockItemRepository.countOfSoldItems();
        if (soldStockItems == null) soldStockItems = 0L;
        soldStockItemCount.setText("" + soldStockItems);
        Double sumOfSoldItems = storageService.sumOfSoldItems();
        soldSum.setText(currencyFormat.format(sumOfSoldItems));

        StringBuilder sb = new StringBuilder();
        if (network != null) {
            sb.append("<html>");
            for (Peer peer: network.getPeers()) {
                sb.append(String.format("%s - %s, dT=%.2f s",
                        peer.getPeerName(), peer.getAddress(), ((double)peer.getTimeDiff())/1000.0));
                sb.append("<br/>");
            }
            sb.append("</html>");
        }
        connections.setText(sb.toString());
    }

    @Override
    public void setVisible(boolean show) {
        if (show) update();
        super.setVisible(show);
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
