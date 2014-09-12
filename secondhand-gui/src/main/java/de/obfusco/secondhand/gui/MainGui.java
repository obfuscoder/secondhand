package de.obfusco.secondhand.gui;

import com.itextpdf.text.DocumentException;
import de.obfusco.secondhand.barcodefilegenerator.BarCodeGeneratorGui;
import de.obfusco.secondhand.net.MessageBroker;
import de.obfusco.secondhand.net.Network;
import de.obfusco.secondhand.net.Peer;
import de.obfusco.secondhand.payoff.gui.PayOffGui;
import de.obfusco.secondhand.receipt.file.ReceiptFile;
import de.obfusco.secondhand.refund.gui.RefundGui;
import de.obfusco.secondhand.sale.gui.CashBoxGui;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import de.obfusco.secondhand.testscan.gui.TestScanGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class MainGui extends JFrame implements MessageBroker, TransactionListener {

    private final static Logger LOG = LoggerFactory.getLogger(MainGui.class);

    Network network;

    @Autowired
    CashBoxGui cashBoxGui;

    @Autowired
    RefundGui refundGui;

    @Autowired
    TestScanGui testScanGui;

    @Autowired
    BarCodeGeneratorGui barCodeGeneratorGui;

    @Autowired
    PayOffGui payOffGui;

    @Autowired
    SearchGui searchGui;

    @Autowired
    ReceiptFile receiptFile;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ReservedItemRepository reservedItemRepository;

    private static final long serialVersionUID = 4961295225628108431L;

    public static final float BUTTON_FONT_SIZE = 25.0f;

    public JButton sale;
    public JButton billGenerator;
    public JButton barcodeGenerator;
    public JButton testScan;
    public JButton createSellerReceipt;
    public JButton createSellerResultReceipt;
    JFileChooser fc;
    JLabel statusLine;

    public MainGui() {
        super("Flohmarkt Kassensystem");
    }

    public void start() {
        if (!initializeNetwork()) return;

        Image image = new ImageIcon("favicon.ico").getImage();
        setIconImage(image);
        addComponentsToPane(getContentPane());
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private boolean initializeNetwork() {
        try {
            network = new Network(31337, this);
            network.start();
        } catch (IOException e) {
            LOG.error("Error while starting network stack!", e);
            return (JOptionPane.showConfirmDialog(null, "Fehler beim Starten der Netzwerkkommunikation.\n" +
                            "Eine Synchronisierung mit anderen Kassen ist nicht möglich.\n" +
                            "Details finden Sie in der Protokolldatei.\n\n" +
                            "Wollen Sie das Programm ohne Netzwerkkommunikation benutzen?",
                    "Netzwerkkommunikation gestört",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION);
        }
        return true;
    }

    private void addComponentsToPane(Container pane) {
        JLabel title = new JLabel("Flohmarkt");
        title.setFont(title.getFont().deriveFont(50.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        fc = new JFileChooser();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 1, 10, 10));

        sale = new JButton("Verkauf");
        sale.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                cashBoxGui.setVisible(true);
            }
        });
        sale.setFont(sale.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton refund = new JButton("Storno");
        refund.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                refundGui.setVisible(true);
            }
        });
        refund.setFont(refund.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton search = new JButton("Artikelsuche");
        search.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                searchGui.open();
            }
        });
        search.setFont(search.getFont().deriveFont(BUTTON_FONT_SIZE));

        billGenerator = new JButton("Abrechnung");
        billGenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                payOffGui.open();
            }
        });
        billGenerator.setFont(billGenerator.getFont().deriveFont(BUTTON_FONT_SIZE));

        barcodeGenerator = new JButton("Barcodes drucken");
        barcodeGenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                barCodeGeneratorGui.setVisible(true);
            }
        });
        barcodeGenerator.setFont(barcodeGenerator.getFont().deriveFont(BUTTON_FONT_SIZE));

        testScan = new JButton("Barcode-Test");
        testScan.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                testScanGui.setVisible(true);
            }
        });
        testScan.setFont(testScan.getFont().deriveFont(BUTTON_FONT_SIZE));

        createSellerReceipt = new JButton("Annahme Verkäuferliste");
        createSellerReceipt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(receiptFile.createFile(Paths.get("data/pdfs/receipts"), "Annahme Flohmarkt",
                            "Mit meiner Unterschrift bestaetige ich die Teilnahmebedingungen."));

                } catch (IOException | DocumentException e1) {
                    LOG.error("Failed to create and open receipts file", e1);
                }
            }
        });
        createSellerReceipt.setFont(createSellerReceipt.getFont().deriveFont(BUTTON_FONT_SIZE));

        createSellerResultReceipt = new JButton("Rückgabe Verkäuferliste");
        createSellerResultReceipt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(receiptFile.createFile(Paths.get("data/pdfs/receipts"), "Rückgabe Verkäufer",
                            "Mit meiner Unterschrift bestätige ich den Erhalt meiner nicht verkauften Artikel sowie meines Gewinnanteils der verkauften Artikel."));

                } catch (IOException | DocumentException e1) {
                    LOG.error("Failed to create and open receipts file", e1);
                }
            }
        });
        createSellerResultReceipt.setFont(createSellerResultReceipt.getFont().deriveFont(BUTTON_FONT_SIZE));

        panel.add(sale);
        panel.add(refund);
        panel.add(search);
        panel.add(createSellerReceipt);
        panel.add(createSellerResultReceipt);
        panel.add(barcodeGenerator);
        panel.add(testScan);
        panel.add(billGenerator);

        statusLine = new JLabel("", SwingConstants.CENTER);
        updateStatusLine();
        panel.add(statusLine);

        pane.add(panel, BorderLayout.SOUTH);
    }

    @Override
    public void messageReceived(Peer peer, String message) {
        LOG.info("Received message from peer " + peer.getAddress() + ": " + message);
        try {
            Transaction transaction = parseTransaction(message);
            synchronized(transactionRepository) {
                if (!transactionRepository.exists(transaction.getId())) {
                    transactionRepository.save(transaction);
                }
            }
        }
        catch (IllegalArgumentException ex) {
            LOG.error("Invalid message <" + message + ">. Reason: " + ex.getMessage());
        }
    }

    private Transaction parseTransaction(String message) {
        String[] messageParts = message.split(";");
        if (messageParts.length != 5) throw new IllegalArgumentException("Message does not contain 5 segments separated by ';'");
        String id = messageParts[0];
        Transaction.Type type = Transaction.Type.valueOf(messageParts[1]);
        Date date = new Date(Long.parseLong(messageParts[2]));
        Integer zipCode = null;
        try { zipCode = Integer.parseInt(messageParts[3]); } catch (NumberFormatException ex) {}
        List<ReservedItem> reservedItems = new ArrayList<>();
        for (String itemId : messageParts[4].split(",")) {
            ReservedItem item = reservedItemRepository.findOne(Integer.parseInt(itemId));
            if (item == null) {
                continue;
            }
            switch (type) {
                case PURCHASE:
                    item.setSold(date);
                    break;
                case REFUND:
                    item.setSold(null);
                    break;
            }
            reservedItems.add(item);
        }
        return Transaction.create(id, date, type, reservedItems, zipCode);
    }

    @Override
    public void connected(final Peer peer) {
        LOG.info("Connected with peer " + peer.getAddress());
        updateStatusLine();
        new Thread(new Runnable() {

            @Override
            public void run() {
                for(Transaction transaction : transactionRepository.findAll(new Sort("created"))) {
                    try {
                        LOG.info("Syncing transaction " + transaction.getId() + " with peer " + peer.getAddress());
                        peer.send(createMessageFromTransaction(transaction));
                    } catch (IOException e) {
                        LOG.error("could not create and send json", e);
                    }
                }
            }
        }).run();
    }

    @Override
    public void disconnected() {
        LOG.info("Disconnected from a peer");
        updateStatusLine();
    }

    private void updateStatusLine() {
        String status = (network == null || network.getNumberOfPeers() == 0) ?
                "Keine Verbindungen mit anderen Systemen" :
                "Verbunden mit " + network.getNumberOfPeers() + " anderen System(en)";
        if (statusLine != null) statusLine.setText(status);
    }

    @Override
    public void notify(Transaction transaction) {
        LOG.info("Notifying all peers about transaction " + transaction.getId());
        try {
            String json = createMessageFromTransaction(transaction);
            network.send(json);
        } catch (IOException ex) {
            LOG.error("Could not create or send json", ex);
        }
    }

    private String createMessageFromTransaction(Transaction transaction) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(transaction.getId()).append(";")
                .append(transaction.getType()).append(";")
                .append(transaction.getCreated().getTime()).append(";")
                .append(transaction.getZipCode()).append(";");
        List<Integer> ids = new ArrayList<>();
        for(ReservedItem reservedItem : transaction.getReservedItems()) {
            ids.add(reservedItem.getId());
        }
        stringBuilder.append(StringUtils.arrayToCommaDelimitedString(ids.toArray()));
        return stringBuilder.toString();
    }
}
