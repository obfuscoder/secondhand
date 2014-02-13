package de.obfusco.secondhand.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.barcodefilegenerator.BarCodeGeneratorGui;
import de.obfusco.secondhand.payoff.gui.PayOffGui;
import de.obfusco.secondhand.postcode.gui.PostCodeGui;
import de.obfusco.secondhand.receipt.file.ReceiptFile;
import de.obfusco.secondhand.refund.gui.RefundGui;
import de.obfusco.secondhand.sale.gui.CashBoxGui;
import de.obfusco.secondhand.testscan.gui.TestScanGui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainGui extends JFrame {

    private final static Logger LOG = LoggerFactory.getLogger(MainGui.class);

    @Autowired
    PostCodeGui postCodeGui;

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
    public JButton billgenerator;
    public JButton barcodegenerator;
    public JButton plzOverview;
    public JButton testScan;
    public JButton createSellerReceipt;
    JFileChooser fc;

    public MainGui() {
        super("Flohmarkt");
        setSize(800, 800);
        setLocation(200, 10);
        addComponentsToPane(getContentPane());
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
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

        plzOverview = new JButton("PLZ Übersicht");
        plzOverview.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                postCodeGui.setVisible(true);
            }
        });
        plzOverview.setFont(plzOverview.getFont().deriveFont(BUTTON_FONT_SIZE));

        billgenerator = new JButton("Abrechnung");
        billgenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                payOffGui.open();
            }
        });
        billgenerator.setFont(billgenerator.getFont().deriveFont(BUTTON_FONT_SIZE));

        barcodegenerator = new JButton("Barcodes drucken");
        barcodegenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                barCodeGeneratorGui.setVisible(true);
            }
        });
        barcodegenerator.setFont(barcodegenerator.getFont().deriveFont(BUTTON_FONT_SIZE));

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
                    Desktop.getDesktop().open(receiptFile.createFile(Paths.get("data/pdfs/receipts")));

                } catch (IOException | DocumentException e1) {
                    LOG.error("Failed to create and open receipts file", e1);
                }
            }
        });
        createSellerReceipt.setFont(createSellerReceipt.getFont().deriveFont(BUTTON_FONT_SIZE));

        panel.add(sale);
        panel.add(refund);
        panel.add(plzOverview);
        panel.add(billgenerator);
        panel.add(barcodegenerator);
        panel.add(testScan);
        panel.add(createSellerReceipt);

        pane.add(panel, BorderLayout.SOUTH);
    }
}
