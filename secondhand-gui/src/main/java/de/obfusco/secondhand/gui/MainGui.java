package de.obfusco.secondhand.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.obfusco.secondhand.barcodefilegenerator.BarCodeGeneratorGui;
import de.obfusco.secondhand.payoff.gui.PayOffGui;
import de.obfusco.secondhand.postcode.gui.PostCodeGui;
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

    private static final long serialVersionUID = 4961295225628108431L;
    public JButton sale;
    public JButton billgenerator;
    public JButton barcodegenerator;
    public JButton plzOverview;
    public JButton testScan;
    JMenuBar menuBar;
    JMenu filemenu;
    JMenuItem itemImportMenu;
    JMenuItem customerImportMenu;
    JMenuItem close;
    JMenuItem importMenu;
    JMenuItem exportMenu;
    JFileChooser fc;
    String customerFilepath = "C:\\flohmarkt\\Customer\\";
    String customerFile = "customer.csv";
    String completeFilepath = "C:\\flohmarkt\\completion\\";
    String soldFilepath = "C:\\flohmarkt\\Sold\\";
    String soldFile = "sold.csv";
    String completefile = "completeItemFile.csv";
    private static final String completeresult = "C:\\flohmarkt\\KomplettAbrechnung\\";
    private static final String completeresultfile = "completesold.csv";

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

        itemImportMenu = new JMenuItem("ArtikelImport");
        itemImportMenu.addActionListener(this);

        customerImportMenu = new JMenuItem("KundenImport");
        customerImportMenu.addActionListener(this);

        close = new JMenuItem("Schließen");
        close.addActionListener(this);

        filemenu.add(itemImportMenu);
        filemenu.add(customerImportMenu);
        filemenu.add(close);

        importMenu = new JMenuItem("Import");
        importMenu.addActionListener(this);
        exportMenu = new JMenuItem("Export");
        exportMenu.addActionListener(this);

        this.setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 6));

        sale = new JButton("Verkauf");
        sale.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                cashBoxGui.setVisible(true);
            }
        });

        plzOverview = new JButton("PLZ Übersicht");
        plzOverview.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                postCodeGui.setVisible(true);
            }
        });

        billgenerator = new JButton("Abrechnung");
        billgenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                payOffGui.open();
            }
        });

        barcodegenerator = new JButton("Barcodes drucken");
        barcodegenerator.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                barCodeGeneratorGui.setVisible(true);
            }
        });

        testScan = new JButton("Barcode- Test");
        testScan.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                testScanGui.setVisible(true);
            }
        });

        panel.add(sale);
        panel.add(plzOverview);
        panel.add(billgenerator);
        panel.add(barcodegenerator);
        panel.add(testScan);

        pane.add(panel, BorderLayout.SOUTH);
    }
}
