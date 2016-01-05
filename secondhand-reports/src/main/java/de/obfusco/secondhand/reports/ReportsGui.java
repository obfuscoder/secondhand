package de.obfusco.secondhand.reports;

import de.obfusco.secondhand.net.Network;
import de.obfusco.secondhand.net.Peer;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
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

    private JLabel itemCount;
    private JLabel transactionCount;
    private JLabel soldCount;
    private JLabel soldSum;
    private JLabel connections;

    private Network network;

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private Set<Peer> peersNeedingHelp = new HashSet<>();

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
        panel.add(new JLabel("Anzahl Transaktionen:"));
        panel.add(transactionCount = new JLabel());
        panel.add(new JLabel("Anzahl verkaufter Artikel:"));
        panel.add(soldCount = new JLabel());
        panel.add(new JLabel("Summe verkaufter Artikel:"));
        panel.add(soldSum = new JLabel());
        panel.add(new JLabel("Verbunden mit:"));
        panel.add(connections = new JLabel());
        contentPane.add(panel);
        pack();
    }

    public void update() {
        itemCount.setText("" + itemRepository.count());
        transactionCount.setText("" + transactionRepository.count());
        soldCount.setText("" + itemRepository.countBySoldNotNull());
        Double sumOfSoldItems = itemRepository.sumOfSoldItems();
        if (sumOfSoldItems == null) sumOfSoldItems = 0.0;
        soldSum.setText(currencyFormat.format(sumOfSoldItems));

        StringBuilder sb = new StringBuilder();
        if (network != null) {
            sb.append("<html>");
            for (Peer peer: network.getPeers()) {
                boolean needsHelp = peersNeedingHelp.contains(peer);
                if (needsHelp) sb.append("<font color=\"red\">");
                sb.append(String.format("%s - %s", peer.getAddress(), peer.getPeerName()));
                if (needsHelp) sb.append("</font>");
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

    public void helpNeeded(Peer peer, boolean isHelpNeeded) {
        if (isHelpNeeded) {
            peersNeedingHelp.add(peer);
        } else {
            peersNeedingHelp.remove(peer);
        }
        update();
    }
}
