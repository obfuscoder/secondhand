package de.obfusco.secondhand.gui;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.itextpdf.text.DocumentException;
import de.obfusco.secondhand.barcodefilegenerator.BarCodeGeneratorGui;
import de.obfusco.secondhand.net.MessageBroker;
import de.obfusco.secondhand.net.Network;
import de.obfusco.secondhand.payoff.gui.PayOffGui;
import de.obfusco.secondhand.receipt.file.ReceiptFile;
import de.obfusco.secondhand.refund.gui.RefundGui;
import de.obfusco.secondhand.sale.gui.CashBoxGui;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.testscan.gui.TestScanGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;

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
    ReceiptFile receiptFile;

    private static final long serialVersionUID = 4961295225628108431L;

    public static final float BUTTON_FONT_SIZE = 25.0f;

    public JButton sale;
    public JButton billGenerator;
    public JButton barcodeGenerator;
    public JButton testScan;
    public JButton createSellerReceipt;
    public JButton createSellerResultReceipt;
    JFileChooser fc;

    public MainGui() {
        super("Flohmarkt Kassensystem");

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
        panel.setLayout(new GridLayout(7, 1, 10, 10));

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

        testScan = new JButton("Barcode- Test");
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
        panel.add(createSellerReceipt);
        panel.add(createSellerResultReceipt);
        panel.add(barcodeGenerator);
        panel.add(testScan);
        panel.add(billGenerator);

        pane.add(panel, BorderLayout.SOUTH);
    }

    @Override
    public String message(String requestMessage) {
        return requestMessage;
    }

    @Override
    public void notify(Transaction transaction) {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            StringWriter stringWriter = new StringWriter();
            JsonGenerator generator = jsonFactory.createGenerator(stringWriter);
            generator.writeStartObject();
            generator.writeObjectField("id", transaction.getId());
            generator.writeObjectField("type", transaction.getType().toString());
            generator.writeObjectField("created", transaction.getCreated().getTime());
            generator.writeObjectField("zip_code", transaction.getZipCode());
            generator.writeArrayFieldStart("reserved_item_ids");
            for(ReservedItem reservedItem : transaction.getReservedItems()) {
                generator.writeNumber(reservedItem.getId());
            }
            generator.writeEndArray();
            generator.writeEndObject();
            generator.close();
            String json = stringWriter.toString();
            LOG.warn("TRANSACTION: " + json);
            network.send("TRANSACTION: " + json);
        } catch (IOException ex) {
            LOG.error("Could not create json", ex);
        }
    }
}
