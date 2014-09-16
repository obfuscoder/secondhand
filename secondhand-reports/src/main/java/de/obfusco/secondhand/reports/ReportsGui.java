package de.obfusco.secondhand.reports;

import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Container;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class ReportsGui extends JFrame {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReservedItemRepository reservedItemRepository;

    private JLabel itemCount;
    private JLabel transactionCount;
    private JLabel soldCount;
    private JLabel soldSum;

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
        panel.add(new JLabel("Anzahl Transaktionen:"));
        panel.add(transactionCount = new JLabel());
        panel.add(new JLabel("Anzahl verkaufter Artikel:"));
        panel.add(soldCount = new JLabel());
        panel.add(new JLabel("Summe verkaufter Artikel:"));
        panel.add(soldSum = new JLabel());
        contentPane.add(panel);
        pack();
    }

    public void update() {
        itemCount.setText("" + reservedItemRepository.count());
        transactionCount.setText("" + transactionRepository.count());
        soldCount.setText("" + reservedItemRepository.countBySoldNotNull());
        soldSum.setText(currencyFormat.format(reservedItemRepository.sumOfSoldItems()));
    }

    @Override
    public void setVisible(boolean show) {
        if (show) update();
        super.setVisible(show);
    }
}
