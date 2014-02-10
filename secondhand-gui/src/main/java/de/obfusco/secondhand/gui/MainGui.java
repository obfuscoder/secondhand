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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.barcodefilegenerator.BarCodeGeneratorGui;
import de.obfusco.secondhand.payoff.gui.PayOffGui;
import de.obfusco.secondhand.postcode.gui.PostCodeGui;
import de.obfusco.secondhand.receipt.file.ReceiptFile;
import de.obfusco.secondhand.sale.gui.CashBoxGui;
import de.obfusco.secondhand.testscan.gui.TestScanGui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainGui extends JFrame implements ActionListener {

    @Autowired
    PostCodeGui postCodeGui;

    @Autowired
    CashBoxGui cashBoxGui;

    @Autowired
    TestScanGui testScanGui;

    @Autowired
    BarCodeGeneratorGui barCodeGeneratorGui;

    @Autowired
    PayOffGui payOffGui;

    @Autowired
    ReceiptFile receiptFile;

    private static final long serialVersionUID = 4961295225628108431L;
    public JButton sale;
    public JButton billgenerator;
    public JButton barcodegenerator;
    public JButton plzOverview;
    public JButton testScan;
    public JButton createSellerReceipt;
    JMenuBar menuBar;
    JMenu filemenu;
    JMenuItem close;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == close) {
            System.exit(0);
        }
    }

    private void addComponentsToPane(Container pane) {
        JLabel title = new JLabel("Flohmarkt");
        title.setFont(title.getFont().deriveFont(50.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        fc = new JFileChooser();

        menuBar = new JMenuBar();
        filemenu = new JMenu("Datei");

        menuBar.add(filemenu);

        close = new JMenuItem("Schließen");
        close.addActionListener(this);

        filemenu.add(close);

        this.setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        sale = new JButton("Verkauf");
        sale.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                cashBoxGui.setVisible(true);
            }
        });
        sale.setFont(sale.getFont().deriveFont(30.0f));

        plzOverview = new JButton("PLZ Übersicht");
        plzOverview.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                postCodeGui.setVisible(true);
            }
        });
        plzOverview.setFont(plzOverview.getFont().deriveFont(30.0f));

        billgenerator = new JButton("Abrechnung");
        billgenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                payOffGui.open();
            }
        });
        billgenerator.setFont(billgenerator.getFont().deriveFont(30.0f));

        barcodegenerator = new JButton("Barcodes drucken");
        barcodegenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                barCodeGeneratorGui.setVisible(true);
            }
        });
        barcodegenerator.setFont(barcodegenerator.getFont().deriveFont(30.0f));

        testScan = new JButton("Barcode- Test");
        testScan.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                testScanGui.setVisible(true);
            }
        });
        testScan.setFont(testScan.getFont().deriveFont(30.0f));

        createSellerReceipt = new JButton("Öffne Annahme Verkäuferliste");
        createSellerReceipt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(receiptFile.createFile(Paths.get("data/pdfs/receipts")));

                } catch (IOException | DocumentException e1) {
                    e1.printStackTrace();
                }
            }
        });
        createSellerReceipt.setFont(createSellerReceipt.getFont().deriveFont(30.0f));

        panel.add(sale);
        panel.add(plzOverview);
        panel.add(billgenerator);
        panel.add(barcodegenerator);
        panel.add(testScan);
        panel.add(createSellerReceipt);

        pane.add(panel, BorderLayout.SOUTH);
    }
}
