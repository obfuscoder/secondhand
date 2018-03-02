package de.obfusco.secondhand.gui;

import com.itextpdf.text.DocumentException;
import de.obfusco.secondhand.gui.config.ConfigGui;
import de.obfusco.secondhand.gui.transactions.TransactionsGui;
import de.obfusco.secondhand.labelgenerator.LabelGeneratorGui;
import de.obfusco.secondhand.net.*;
import de.obfusco.secondhand.payoff.gui.PayOffGui;
import de.obfusco.secondhand.receipt.file.ReceiptFile;
import de.obfusco.secondhand.refund.gui.RefundGui;
import de.obfusco.secondhand.reports.ReportsGui;
import de.obfusco.secondhand.sale.gui.CashBoxGui;
import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.*;
import de.obfusco.secondhand.storage.repository.*;
import de.obfusco.secondhand.storage.service.StorageService;
import de.obfusco.secondhand.sync.PathSyncListener;
import de.obfusco.secondhand.sync.PathSyncer;
import de.obfusco.secondhand.testscan.gui.TestScanGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Component
public class MainGui extends JFrame implements MessageBroker, TransactionListener, DataPusher, PathSyncListener {

    private static final float BUTTON_FONT_SIZE = 25.0f;
    private final static Logger LOG = LoggerFactory.getLogger(MainGui.class);
    private static final long serialVersionUID = 4961295225628108431L;
    private JToggleButton helpButton;
    private Network network;
    @Autowired
    CashBoxGui cashBoxGui;
    @Autowired
    RefundGui refundGui;
    @Autowired
    TestScanGui testScanGui;
    @Autowired
    LabelGeneratorGui labelGeneratorGui;
    @Autowired
    PayOffGui payOffGui;
    @Autowired
    de.obfusco.secondhand.gui.SearchGui searchGui;
    @Autowired
    ReportsGui reportsGui;
    @Autowired
    ReceiptFile receiptFile;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ConfigGui configGui;
    private JLabel statusLabel;
    private JLabel folderSyncLabel;
    private Properties properties = new Properties();
    @Autowired
    private StorageConverter storageConverter;
    private NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    @Autowired
    private TransactionsGui transactionsGui;
    @Autowired
    private PathSyncer pathSyncer;
    @Autowired
    private StorageService storageService;
    private int pathSyncErrorCount;

    private JsonDataConverter converter = new JsonDataConverter();

    public MainGui() {
        super("Flohmarkt Kassensystem");
        loadProperties();
    }

    private void loadProperties() {
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            properties.load(input);
        } catch (FileNotFoundException ex) {
            LOG.warn("Could not load properties");
        } catch (IOException ex) {
            LOG.error("Error while reading properties file", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.error("Could not close properties file", e);
                }
            }
        }
    }

    public void start() {
        Image image = new ImageIcon("favicon.ico").getImage();
        setIconImage(image);
        addComponentsToPane(getContentPane());
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        importDataWhenPresent();
        if (!initializeNetwork()) return;
        startPathSync();
        setVisible(true);
    }

    private void importDataWhenPresent() {
        File importFile = new File("flohmarkthelfer.data");
        if (!importFile.canRead()) {
            return;
        }
        int dialogResult = JOptionPane.showConfirmDialog(this, "Es wurden Daten zum Importieren gefunden.\n" +
                "Sollen diese importiert werden?\n" +
                "Sollten lokal bereits Verkäufe getätigt worden sein,\ndie noch nicht zum Online-System " +
                "synchronisiert wurden,\ngehen diese verloren.", "Daten importieren", JOptionPane.YES_NO_OPTION);
        if (dialogResult != JOptionPane.YES_OPTION) {
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(importFile)) {
            de.obfusco.secondhand.net.dto.Event event = converter.parseCompressedStream(fileInputStream);
            storageConverter.storeEvent(event);
            JOptionPane.showMessageDialog(this, "Daten erfolgreich importiert", "Import erfolgreich", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Die Daten konnten leider nicht erfolgreich importiert werden", "Importfehler", JOptionPane.WARNING_MESSAGE);
            LOG.error("Error while importing data from disk", e);
            return;
        }
        importFile.delete();
    }

    private void startPathSync() {
        String localName = "LOCAL";
        try {
            localName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.warn("Could not get local host name", e);
        }
        pathSyncer.synchronize(properties.getProperty("sync.path", "E:\\"), localName);
    }

    private boolean initializeNetwork() {
        try {
            String name = properties.getProperty("peer.name", InetAddress.getLocalHost().getHostName());
            network = new Network(31454, this, name);
            network.start();
            reportsGui.setNetwork(network);
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
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 10, 10));

        JButton sale = new JButton("Verkauf");
        sale.addActionListener(e -> cashBoxGui.setVisible(true));
        sale.setFont(sale.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton refund = new JButton("Storno");
        refund.addActionListener(e -> refundGui.setVisible(true));
        refund.setFont(refund.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton search = new JButton("Artikelsuche");
        search.addActionListener(e -> searchGui.open());
        search.setFont(search.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton billGenerator = new JButton("Abrechnung");
        billGenerator.addActionListener(e -> payOffGui.open());
        billGenerator.setFont(billGenerator.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton testScan = new JButton("Barcode-Test");
        testScan.addActionListener(e -> testScanGui.setVisible(true));
        testScan.setFont(testScan.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton barcodeGenerator = new JButton("Barcodes drucken");
        barcodeGenerator.addActionListener(e -> labelGeneratorGui.setVisible(true));
        barcodeGenerator.setFont(barcodeGenerator.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton createSellerReceipt = new JButton("Annahme Verkäuferliste");
        createSellerReceipt.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(receiptFile.createFile(Paths.get("data/pdfs/receipts"),
                        "Annahme Flohmarkt",
                        "Mit meiner Unterschrift bestätige ich die Teilnahmebedingungen."));

            } catch (IOException | DocumentException e1) {
                LOG.error("Failed to create and open receipts file", e1);
            }
        });
        createSellerReceipt.setFont(createSellerReceipt.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton createSellerResultReceipt = new JButton("Rückgabe Verkäuferliste");
        createSellerResultReceipt.addActionListener(e -> {
            try {
                createPayoutReceipt();
            } catch (IOException | DocumentException e1) {
                LOG.error("Failed to create and open receipts file", e1);
            }
        });
        createSellerResultReceipt.setFont(createSellerResultReceipt.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton reportsButton = new JButton("Statistiken");
        reportsButton.addActionListener(e -> reportsGui.setVisible(true));
        reportsButton.setFont(reportsButton.getFont().deriveFont(BUTTON_FONT_SIZE));

        helpButton = new JToggleButton("Hilfe");
        helpButton.addItemListener(itemEvent -> helpNeeded(itemEvent.getStateChange() == ItemEvent.SELECTED));
        helpButton.setFont(helpButton.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton configButton = new JButton("Einstellungen");
        configButton.addActionListener(e -> {
            configGui.setRootUrl(properties.getProperty("online.root"));
            configGui.setVisible(true);
        });
        configButton.setFont(configButton.getFont().deriveFont(BUTTON_FONT_SIZE));

        JButton transactionsButton = new JButton("Transaktionen");
        transactionsButton.addActionListener(arg -> transactionsGui.setVisible(true));
        transactionsButton.setFont(transactionsButton.getFont().deriveFont(BUTTON_FONT_SIZE));

        panel.add(sale);
        panel.add(refund);
        panel.add(search);

        boolean showAllButtons = "true".equals(properties.getProperty("buttons.all"));
        if (showAllButtons) {
            panel.add(createSellerReceipt);
            panel.add(createSellerResultReceipt);
            panel.add(testScan);
            panel.add(barcodeGenerator);
            panel.add(billGenerator);
            panel.add(configButton);
            panel.add(transactionsButton);
        }
        panel.add(reportsButton);
        panel.add(helpButton);

        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("", SwingConstants.CENTER);
        folderSyncLabel = new JLabel("SYNC", SwingConstants.RIGHT);
        updateStatusLabel();
        updateFolderSyncLabel(SyncStatus.OFFLINE);
        statusPanel.add(statusLabel);
        statusPanel.add(folderSyncLabel);
        panel.add(statusPanel);

        pane.add(panel, BorderLayout.SOUTH);
    }

    private void updateFolderSyncLabel(SyncStatus syncStatus) {
        switch(syncStatus) {
            case OFFLINE:
                folderSyncLabel.setForeground(Color.RED);
                break;
            case ONLINE:
                folderSyncLabel.setForeground(Color.GREEN.darker());
                break;
            case ONGOING:
                folderSyncLabel.setForeground(Color.BLUE);

                break;
        }
        folderSyncLabel.setText(String.format("SYNC (%d)", pathSyncErrorCount));
    }

    private void createPayoutReceipt() throws IOException, DocumentException {
        boolean withPayouts = JOptionPane.showConfirmDialog(
                null, "Soll eine Spalte für den Auszahlbetrag mit enthalten sein?", "Auszahlbetrag",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
        File file;
        Path fileBasePath = Paths.get("data/pdfs/receipts");
        String title = "Rückgabe Verkäufer";
        String introText = "Mit meiner Unterschrift bestätige ich den Erhalt meiner nicht verkauften Artikel sowie meines Gewinnanteils der verkauften Artikel.";
        if (withPayouts) {
            Map<Integer, String> payouts = new HashMap<>();
            Event event = eventRepository.find();
            double pricePrecision = event.pricePrecision.doubleValue();
            for (Reservation reservation : reservationRepository.findAll()) {
                double sum = 0;
                for (Item item : itemRepository.findByReservationAndSoldNotNullOrderByNumberAsc(reservation)) {
                    sum += item.price.doubleValue();
                }
                double commissionCutSum = sum * (1 - reservation.commissionRate.doubleValue());
                commissionCutSum = Math.floor(commissionCutSum / pricePrecision) * pricePrecision;
                if (event.incorporateReservationFee()) {
                    commissionCutSum -= reservation.fee.doubleValue();
                }
                payouts.put(reservation.number, currency.format(commissionCutSum));
            }
            file = receiptFile.createFile(fileBasePath, title, introText, "Betrag", payouts);
        } else {
            file = receiptFile.createFile(fileBasePath, title, introText, null, null);
        }
        Desktop.getDesktop().open(file);
    }

    private void helpNeeded(boolean isNeeded) {
        if (network == null) return;
        network.send("HELP-" + (isNeeded ? "ON" : "OFF"));
    }

    @Override
    public void messageReceived(Peer peer, String message) {
        LOG.info("Received message from peer " + peer.getAddress() + ": " + message);
        try {
            if (message.startsWith("HELP")) {
                parseHelpMessage(peer, message);
            } else if(message.startsWith("DATA")) {
                parseDataMessage(peer, message);
            } else {
                transactionReceived(storageService.parseTransactionMessage(message));
            }
        }
        catch (IllegalArgumentException ex) {
            LOG.error("Invalid message <" + message + ">. Reason: " + ex.getMessage());
        }
    }

    public void transactionReceived(Transaction transaction) {
        if (transaction != null) {
            LOG.debug("Received transaction: {}", transaction);
            synchronized (transactionRepository) {
                if (!transactionRepository.exists(transaction.id)) {
                    transactionRepository.save(transaction);
                    reportsGui.update();
                } else {
                    LOG.debug("Skipping transaction {} as it is already known", transaction.id);
                }
            }
        }
    }

    private void parseDataMessage(Peer peer, String message) {
        try {
            de.obfusco.secondhand.net.dto.Event event = converter.parseBase64Compressed(message.substring(4));
            storageConverter.storeEvent(event);
            JOptionPane.showMessageDialog(this,
                    String.format("Daten erfolgreich von [%s] importiert", peer.getPeerName()),
                    "Import erfolgreich", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            LOG.error("Could not properly parse DATA message", e);
        }
    }

    private void parseHelpMessage(Peer peer, String message) {
        String[] parts = message.split("-");
        reportsGui.helpNeeded(peer, parts.length == 2 && parts[1].equals("ON"));
    }

    @Override
    public void connected(final Peer peer) {
        LOG.info("Connected with peer " + peer.getAddress());
        updateStatusLabel();
        reportsGui.update();
        peer.send("HELP-" + (helpButton.isSelected() ? "ON" : "OFF"));
        new Thread(() -> {
            for (Transaction transaction : transactionRepository.findAll(new Sort("created"))) {
                LOG.info("Syncing transaction " + transaction.id + " with peer " + peer.getAddress());
                peer.send(transaction.toString());
            }
        }).start();
    }

    @Override
    public void disconnected() {
        LOG.info("Disconnected from a peer");
        updateStatusLabel();
        reportsGui.update();
    }

    private void updateStatusLabel() {
        if (network == null) {
            statusLabel.setText("Keine Verbindung");
            return;
        }
        statusLabel.setText(String.format("Verbunden mit %d System(en)", network.getNumberOfPeers()));
    }

    @Override
    public void notify(Transaction transaction) {
        LOG.info("Notifying all peers about transaction " + transaction.id);
        String message = transaction.toString();
        if (network != null) network.send(message);
        reportsGui.update();
    }

    @Override
    public void push(de.obfusco.secondhand.net.dto.Event event) throws IOException {
        if (network == null) throw new IOException("Network is not available.");
        network.send("DATA" + converter.toBase64CompressedJson(event));
    }

    @Override
    public void syncPathNotAvailable() {
        updateFolderSyncLabel(SyncStatus.OFFLINE);
    }

    @Override
    public void syncPathAvailable() {
        updateFolderSyncLabel(SyncStatus.ONLINE);
    }

    @Override
    public void synchronizationStarted() {
        updateFolderSyncLabel(SyncStatus.ONGOING);
    }

    @Override
    public void synchronizationFinished() {
        updateFolderSyncLabel(SyncStatus.ONLINE);
    }

    @Override
    public void synchronizationError() {
        pathSyncErrorCount ++;
    }

    enum SyncStatus {
        OFFLINE,
        ONLINE,
        ONGOING
    }
}
