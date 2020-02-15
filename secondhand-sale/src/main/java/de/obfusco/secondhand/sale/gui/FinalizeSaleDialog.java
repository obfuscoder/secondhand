package de.obfusco.secondhand.sale.gui;

import com.itextpdf.text.DocumentException;
import de.obfusco.secondhand.storage.model.BaseItem;
import de.obfusco.secondhand.storage.model.Transaction;
import de.obfusco.secondhand.storage.model.TransactionListener;
import de.obfusco.secondhand.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

class FinalizeSaleDialog extends JDialog implements ActionListener {
    private static NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private final static Logger LOG = LoggerFactory.getLogger(FinalizeSaleDialog.class);

    private static final long serialVersionUID = -9004809235134991240L;

    private JLabel priceLabel;
    private JLabel changeBarlabel;
    private JTextField barTextField;
    private JFormattedTextField postCodeTextField;
    private BigDecimal change;
    private JLabel errorLabel;
    private BigDecimal finalPrice;

    private JButton okButton = new JButton("OK");
    private JButton cancelButton = new JButton("Cancel");
    private JButton printButton = new JButton("OK + Drucken");

    private JLabel title = new JLabel("Verkauf abschließen");

    private SaleDialog frame;
    private BigDecimal priceFactor;

    private StorageService storageService;
    private List<BaseItem> items;

    private boolean showPostCode;

    private String postCode = null;
    private Path basePath = Paths.get("data/pdfs/sale");
    private TransactionListener transactionListener;

    public FinalizeSaleDialog(SaleDialog parentFrame, TransactionListener orderListener) {
        super(parentFrame, "Verkauf abschließen", true);
        frame = parentFrame;
        priceFactor = frame.eventRepository.find().priceFactor;
        finalPrice = (priceFactor == null) ? parentFrame.totalPrice : parentFrame.totalPrice.multiply(priceFactor);
        storageService = frame.getStorageService();
        items = frame.getTableData();

        Properties properties = new Properties();
        loadProperties(properties);
        showPostCode = "true".equals(properties.getProperty("postcode"));

        this.transactionListener = orderListener;
        setSize(400, 300);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 0, 0, 255));

        buildNewObjectDialog();
    }

    private void loadProperties(Properties properties) {
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            properties.load(input);
        } catch (IOException ex) {
            LOG.warn("Could not load properties", ex);
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

    private void buildNewObjectDialog() {
        setFont(getFont().deriveFont(20f));

        setLayout(new BorderLayout());

        title.setFont(title.getFont().deriveFont(24.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(8, 0));

        contentPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel sumPanel = new JPanel(new GridLayout(0, 2));
        JLabel sumLabel = new JLabel("SUMME: ");
        priceLabel = new JLabel(CURRENCY.format(frame.totalPrice));
        sumPanel.add(sumLabel);
        sumPanel.add(priceLabel);
        contentPanel.add(sumPanel);

        if (priceFactor == null) {
            sumLabel.setFont(title.getFont().deriveFont(20.0f));
            priceLabel.setFont(title.getFont().deriveFont(20.0f));
        } else {
            JPanel finalSumPanel = new JPanel(new GridLayout(0, 2));
            JLabel finalSumLabel = new JLabel("ENDSUMME: ");
            JLabel finalPriceLabel = new JLabel(CURRENCY.format(finalPrice));
            finalSumPanel.add(finalSumLabel);
            finalSumPanel.add(finalPriceLabel);
            contentPanel.add(finalSumPanel);

            finalSumLabel.setFont(title.getFont().deriveFont(20.0f));
            finalPriceLabel.setFont(title.getFont().deriveFont(20.0f));
        }

        JLabel barLabel = new JLabel("BAR (optional)");
        barTextField = new JTextField();
        barTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    calculateChange();
                    if (showPostCode) {
                        postCodeTextField.requestFocus();
                    } else {
                        okButton.requestFocus();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    frame.getItemNr().requestFocus();
                    dispose();
                }

            }

        });

        postCodeTextField = new JFormattedTextField();
        postCodeTextField.setColumns(5);
        postCodeTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    getPostcode();
                    okButton.requestFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    frame.getItemNr().requestFocus();
                    dispose();
                }

            }

        });

        JLabel changeLabel = new JLabel("RÜCKGELD");
        changeBarlabel = new JLabel(CURRENCY.format(BigDecimal.ZERO));
        changeBarlabel.setForeground(Color.red);
        changeBarlabel.setFont(getFont());

        JPanel barPanel = new JPanel(new GridLayout(0, 2));
        barPanel.add(barLabel);
        barPanel.add(barTextField);

        JPanel changePanel = new JPanel(new GridLayout(0, 2));
        changePanel.add(changeLabel);
        changePanel.add(changeBarlabel);

        contentPanel.add(barPanel);
        contentPanel.add(changePanel);
        if (showPostCode) {
            JPanel postCodePanel = new JPanel(new GridLayout(0, 3));
            postCodePanel.add(new JLabel("PLZ (optional)"));
            postCodePanel.add(postCodeTextField);

            contentPanel.add(new JPanel());
            contentPanel.add(postCodePanel);
        }
        contentPanel.add(new JPanel());

        this.add(contentPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));

        bottomPanel.add(errorLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 3));

        okButton.addActionListener(this);
        okButton.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    completeOrder();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                    frame.getItemNr().requestFocus();
                }
            }
        });
        cancelButton.addActionListener(this);
        printButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(printButton);

        bottomPanel.add(buttonPanel);

        this.add(bottomPanel, BorderLayout.SOUTH);

    }

    private boolean getPostcode() {
        String postText = postCodeTextField.getText();
        postCode = null;
        if (postText.length() == 5) {
            try {
                postCode = postText;
                errorLabel.setText("");
                return true;
            } catch (NumberFormatException ex) {
                errorLabel.setText("Ungültige PLZ. Bitte nur Zahlen eingeben.");
                postCodeTextField.requestFocus();
                return false;
            }
        } else if (postText.length() != 0) {
            errorLabel.setText("Ungültige PLZ. 5 Stellen bitte.");
            postCodeTextField.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == okButton) {
            completeOrder();
        } else if (e.getSource() == cancelButton) {
            this.dispose();
        } else if (e.getSource() == printButton) {
            try {
                File pdfFile = new BillPDFCreator().createPdf(basePath, frame.getTableData(), finalPrice);
                Desktop.getDesktop().open(pdfFile);
                completeOrder();
            } catch (DocumentException | IOException ex) {
                JOptionPane.showMessageDialog(this, "Fehler",
                        "Es trat ein Fehler beim Erstellen der Rechnung auf.",
                        JOptionPane.ERROR_MESSAGE);
                LOG.error("Error while creating receipt", ex);
            }
        }
    }

    private void completeOrder() {

        calculateChange();

        boolean postcodeOK = true;
        if (postCode == null && postCodeTextField.getText().length() > 0) {
            postcodeOK = getPostcode();
        }
        if (!postcodeOK) {
            return;
        }

        List<String> itemCodes = items.stream().map(it -> it.code).collect(Collectors.toList());
        Transaction transaction = storageService.storeSoldInformation(itemCodes, postCode);
        transactionListener.notify(transaction);

        frame.newCustomer();
        this.dispose();
    }

    private void calculateChange() {

        if (barTextField.getText() == null || barTextField.getText().equals("")) {
            return;
        }
        BigDecimal bar = new BigDecimal(barTextField.getText().replace(",", "."));
        change = bar.subtract(finalPrice);

        String back = CURRENCY.format(change);
        changeBarlabel.setText(back);
    }

}
