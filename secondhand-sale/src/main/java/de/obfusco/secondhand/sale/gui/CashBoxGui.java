package de.obfusco.secondhand.sale.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.sale.service.StorageService;
import de.obfusco.secondhand.storage.model.ReservedItem;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashBoxGui extends JFrame implements ActionListener {

    private static final long serialVersionUID = -698049510249510666L;
    JTextField itemNr;
    CashTableModel tablemodel;
    JLabel errorLabel;
    JLabel priceLabel;
    JTable cashTable;
    JLabel changeBarlabel;
    JTextField barTextField;
    JFormattedTextField postCodeTextField;
    String sum;
    String change;

    @Autowired
    StorageService storageService;

    JButton readyButton = new JButton("Fertig");
    JButton newButton = new JButton("Neuer Kunde");
    JButton printButton = new JButton("Drucken");

    private static final String PRINTTO = "C:\\flohmarkt\\bill.pdf";
    private static final String PRINTTOIMG = "C:\\flohmarkt\\billimage";

    public CashBoxGui() {
        super("Flohmarkt Verkauf");
        setSize(600, 600);
        addComponentsToPane(getContentPane());
        pack();
        setLocationRelativeTo(null);
    }

    private void addComponentsToPane(Container pane) {

        JLabel title = new JLabel("Flohmarkt Verkauf");
        title.setFont(title.getFont().deriveFont(24.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 0, 0, 255));

        tablemodel = new CashTableModel();

        cashTable = new JTable(tablemodel);
        cashTable.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedRow();
                }

            }
        });

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        cashTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        itemNr = new JTextField();
        itemNr.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String itemText = itemNr.getText();
                    if (itemText.length() != 8) {
                        setErrorText("Artikelnummer "
                                + itemNr.getText()
                                + " ist falsch! Die Nummer muss 8 Zeichen lang sein!");

                        itemNr.setText("");
                        return;
                    }

                    if (tablemodel.findItemNr(itemNr.getText())) {
                        setErrorText("Artikelnummer " + itemNr.getText()
                                + " bereits eingescannt!");
                        itemNr.setText("");
                        return;
                    }

                    addItem();
                }

            }
        });

        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.add(itemNr, BorderLayout.NORTH);
        itemPanel.add(new JScrollPane(cashTable), BorderLayout.CENTER);
        // itemPanel.add(removeButton, BorderLayout.EAST);
        itemPanel.add(errorLabel, BorderLayout.SOUTH);

        JLabel sumLabel = new JLabel("SUMME: ");
        priceLabel = new JLabel("0");
        JLabel sumeuroLabel = new JLabel("Euro");
        JPanel sumPanel = new JPanel(new GridLayout(0, 3));
        sumLabel.setFont(title.getFont().deriveFont(20.0f));
        priceLabel.setFont(title.getFont().deriveFont(20.0f));
        sumeuroLabel.setFont(title.getFont().deriveFont(20.0f));
        sumPanel.add(sumLabel);
        sumPanel.add(priceLabel);
        sumPanel.add(sumeuroLabel);

        pane.add(itemPanel, BorderLayout.CENTER);

        JLabel bareuroLabel = new JLabel("Euro");
        JLabel barLabel = new JLabel("BAR (optinal)");
        barTextField = new JTextField();
        barTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    calculateChange();
                }

            }

        });

        postCodeTextField = new JFormattedTextField();
        postCodeTextField.setColumns(5);

        JLabel changeeuroLabel = new JLabel("Euro");
        JLabel changeLabel = new JLabel("RÜCKGELD");
        changeBarlabel = new JLabel("0,00");
        changeBarlabel.setForeground(Color.red);

        JLabel postCodeLabel = new JLabel("PLZ (optional)");

        JPanel barPanel = new JPanel(new GridLayout(0, 3));
        barPanel.add(barLabel);
        barPanel.add(barTextField);
        barPanel.add(bareuroLabel);

        JPanel changePanel = new JPanel(new GridLayout(0, 3));
        changePanel.add(changeLabel);
        changePanel.add(changeBarlabel);
        changePanel.add(changeeuroLabel);

        JPanel postCodePanel = new JPanel(new GridLayout(0, 3));
        postCodePanel.add(postCodeLabel);
        postCodePanel.add(postCodeTextField);

        newButton.setEnabled(false);
        printButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 3));

        buttonPanel.add(readyButton);
        buttonPanel.add(newButton);
        buttonPanel.add(printButton);

        readyButton.addActionListener(this);
        newButton.addActionListener(this);
        printButton.addActionListener(this);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(7, 0));
        southPanel.add(sumPanel);
        southPanel.add(barPanel);
        southPanel.add(changePanel);
        southPanel.add(new JPanel());
        southPanel.add(postCodePanel);
        southPanel.add(new JPanel());
        southPanel.add(buttonPanel);

        pane.add(southPanel, BorderLayout.SOUTH);
    }

    private void calculateChange() {

        if (barTextField.getText() == null || barTextField.getText().equals("")) {
            return;
        }
        float bar = Float
                .parseFloat((barTextField.getText()).replace(",", "."));
        float prise = Float
                .parseFloat((priceLabel.getText()).replace(",", "."));
        float back = bar * 100 - prise * 100;
        back /= 100;

        change = String.format("%.2f", back);
        changeBarlabel.setText(change);
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == readyButton) {

            errorLabel.setText("");
            String postText = postCodeTextField.getText();
            int postCode = 0;
            if (postText.length() != 0 && postText.length() != 0) {
                errorLabel.setText("Ungültige PLZ. 5 Stellen bitte.");
                return;
            } else {
                if (postText.length() == 5) {
                    try {
                        postCode = Integer.parseInt(postText);
                        errorLabel.setText("");
                    } catch (NumberFormatException ex) {
                        errorLabel
                                .setText("Ungültige PLZ. Bitte nur Zahlen eingeben.");
                        return;
                    }
                }
            }
            calculateChange();
            storageService.storeSoldInformation(tablemodel.getColumnData(0), postCode);

            newButton.setEnabled(true);
            printButton.setEnabled(true);
            readyButton.setEnabled(false);
            itemNr.setEnabled(false);
            postCodeTextField.setEnabled(false);
            barTextField.setEnabled(false);
            cashTable.setEnabled(false);

        } else if (event.getSource() == newButton) {

            newButton.setEnabled(false);
            printButton.setEnabled(false);
            readyButton.setEnabled(true);
            itemNr.setEnabled(true);
            postCodeTextField.setEnabled(true);
            barTextField.setEnabled(true);
            cashTable.setEnabled(true);

            itemNr.setText("");
            int rowCount = tablemodel.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                tablemodel.delRow(0);
            }

            priceLabel.setText("0,00");
            barTextField.setText("");
            changeBarlabel.setText("0,00");
            postCodeTextField.setText("");
            validate();
            pack();

            itemNr.requestFocus();
        } else if (event.getSource() == printButton) {
            try {
                String bar = "";
                if (barTextField.getText() != null
                        && !barTextField.getText().equals("")) {
                    bar = String.format("%.2f",
                            Float.parseFloat(barTextField.getText()));
                } else {
                    bar = String.format("%.2f",
                            Float.parseFloat(sum.replace(",", ".")));
                }
                new BillPDFCreator().createPdf(PRINTTO, tablemodel.getData(),
                        sum, bar, change);
            } catch (IOException | DocumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            PDDocument document = null;
            try {
                document = PDDocument.load(PRINTTO);
                int pages = document.getPageCount();
                PDFImageWriter writer = new PDFImageWriter();
                boolean success = writer.writeImage(document, "jpg", "", 1, 1,
                        PRINTTOIMG, BufferedImage.TYPE_INT_RGB, Toolkit
                        .getDefaultToolkit().getScreenResolution());

                DocFlavor flavor = DocFlavor.INPUT_STREAM.JPEG;
                PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
                PrintService pservice = PrintServiceLookup
                        .lookupDefaultPrintService();
                DocPrintJob pj = pservice.createPrintJob();
                for (int i = 0; i < pages; i++) {
                    try {
                        FileInputStream fis = new FileInputStream(PRINTTOIMG
                                + (i + 1) + ".jpg");
                        Doc doc = new SimpleDoc(fis, flavor, null);
                        pj.print(doc, aset);
                    } catch (FileNotFoundException fe) {
                    } catch (PrintException e) {
                        System.out.println("Fehler beim drucken: " + e);
                    }
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private static class App {

        public App() {
        }
    }

    class CashTableModel extends AbstractTableModel {

        private List<String> columnNames = new ArrayList<>(Arrays.asList(
                "ArtNr", "Kategorie", "Bezeichnung", "Groesse", "Preis"));

        private List<Object> data = new ArrayList<>();

        public List<Object> getData() {
            return data;
        }

        public List<String> getColumnData(int col) {
            List<String> columnData = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                Object[] row = (Object[]) data.get(i);
                columnData.add(row[col].toString());
            }

            return columnData;
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            Object[] itemrow = (Object[]) data.get(row);

            return itemrow[col];
        }

        @Override
        public String getColumnName(int index) {
            return (String) columnNames.get(index);
        }

        public Boolean findItemNr(String nr) {

            for (int i = 0; i < getRowCount(); i++) {
                // System.out.println("Value at " + i + " 0 = \"" +
                // getValueAt(i, 0) + "\"" + " == \"" + nr + "\"");
                if (getValueAt(i, 0).equals(nr)) {
                    return true;
                }
            }
            return false;
        }

        public void addRow(Object[] row) {

            if (data.contains(row)) {
                setErrorText("Artikel schon vorhanden!");
            } else {
                data.add(row);
                addToSum(Float.parseFloat(((String) row[4]).replace(",", ".")));
                this.fireTableDataChanged();
            }
        }

        public void delRow(int row) {

            Object[] itemrow = (Object[]) data.get(row);
            addToSum(Float.parseFloat(((String) itemrow[4]).replace(",", "."))
                    * -1);
            data.remove(row);
            this.fireTableDataChanged();

        }

    }

    public void addItem() {
        String code = itemNr.getText();
        itemNr.setText("");
        setErrorText(" ");
        Object[] data = null;
        ReservedItem reservedItem;
        reservedItem = storageService.getReservedItem(code);
        if (reservedItem == null) {
            setErrorText("Artikel mit Nummer \"" + code
                    + "\" existiert nicht!");
            return;
        }
        if (reservedItem.isSold()) {
            setErrorText("Artikel mit Nummer \"" + code
                    + "\" wurde bereits verkauft!");
            return;
        }
        data = new Object[]{reservedItem.getCode(), reservedItem.getItem().getCategory().getName(), reservedItem.getItem().getDescription(), reservedItem.getItem().getSize(), reservedItem.getItem().getPrice().toString()};
        tablemodel.addRow(data);
    }

    public void setErrorText(String text) {
        errorLabel.setText(text);
        errorLabel.getParent().invalidate();
        errorLabel.getParent().validate();
        this.validate();
        this.pack();
    }

    float totalPrise = (float) 0.0;

    public void addToSum(float prise) {
        totalPrise = totalPrise * 100 + prise * 100;
        totalPrise /= 100;
        sum = String.format("%.2f", totalPrise);
        priceLabel.setText(sum);
        priceLabel.getParent().invalidate();
        priceLabel.getParent().validate();
        this.validate();
        this.pack();
    }

    public void deleteSelectedRow() {
        int n = JOptionPane.showConfirmDialog(
                this,
                "Möchten sie den Artikel \""
                + tablemodel.getValueAt(cashTable.getSelectedRow(), 0)
                + "\" wirklich löschen?", "Artikel löschen",
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
            tablemodel.delRow(cashTable.getSelectedRow());
        }

        itemNr.requestFocus();

    }
}
