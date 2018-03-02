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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

class CheckOutDialog extends JDialog implements ActionListener {

    private final static Logger LOG = LoggerFactory.getLogger(CheckOutDialog.class);

    private static final long serialVersionUID = -9004809235134991240L;

    private JLabel priceLabel;
    private JLabel changeBarlabel;
    private JTextField barTextField;
    private JFormattedTextField postCodeTextField;
    private Double change;
    private JLabel errorLabel;

    private JButton okButton = new JButton("OK");
    private JButton cancelButton = new JButton("Cancel");
    private JButton printButton = new JButton("OK + Drucken");

    private JLabel title = new JLabel("Verkauf abschließen");

    private CashBoxGui frame;

    private StorageService storageService;
    private List<BaseItem> items;

    private boolean showPostCode;

    private String postCode = null;
    private Path basePath = Paths.get("data/pdfs/sale");
    private TransactionListener transactionListener;

    public CheckOutDialog(JFrame parentFrame, TransactionListener orderListener) {
        super(parentFrame, "Verkauf abschließen", true);

        Properties properties = new Properties();
        loadProperties(properties);
        showPostCode = "true".equals(properties.getProperty("postcode"));

        this.transactionListener = orderListener;
        setSize(400, 300);

        frame = (CashBoxGui) parentFrame;
        this.storageService = frame.getStorageService();
        this.items = frame.getTableData();
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

        JPanel checkOutPanel = new JPanel(new GridLayout(8, 0));

        checkOutPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        JLabel sumLabel = new JLabel("SUMME: ");
        priceLabel = new JLabel(frame.getPrice());
        JLabel sumeuroLabel = new JLabel("Euro");
        JPanel sumPanel = new JPanel(new GridLayout(0, 3));
        sumLabel.setFont(title.getFont().deriveFont(20.0f));
        priceLabel.setFont(title.getFont().deriveFont(20.0f));
        sumeuroLabel.setFont(title.getFont().deriveFont(20.0f));
        sumPanel.add(sumLabel);
        sumPanel.add(priceLabel);
        sumPanel.add(sumeuroLabel);

        JLabel bareuroLabel = new JLabel("Euro");
        JLabel barLabel = new JLabel("BAR (optinal)");
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

        JLabel changeeuroLabel = new JLabel("Euro");
        JLabel changeLabel = new JLabel("RÜCKGELD");
        changeBarlabel = new JLabel("0,00");
        changeBarlabel.setForeground(Color.red);
        changeBarlabel.setFont(getFont());

        JPanel barPanel = new JPanel(new GridLayout(0, 3));
        barPanel.add(barLabel);
        barPanel.add(barTextField);
        barPanel.add(bareuroLabel);

        JPanel changePanel = new JPanel(new GridLayout(0, 3));
        changePanel.add(changeLabel);
        changePanel.add(changeBarlabel);
        changePanel.add(changeeuroLabel);

        checkOutPanel.add(sumPanel);
        checkOutPanel.add(barPanel);
        checkOutPanel.add(changePanel);
        if (showPostCode) {
            JPanel postCodePanel = new JPanel(new GridLayout(0, 3));
            postCodePanel.add(new JLabel("PLZ (optional)"));
            postCodePanel.add(postCodeTextField);

            checkOutPanel.add(new JPanel());
            checkOutPanel.add(postCodePanel);
        }
        checkOutPanel.add(new JPanel());

        this.add(checkOutPanel, BorderLayout.CENTER);

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
                File pdfFile = new BillPDFCreator().createPdf(basePath, frame.getTableData());
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
        Double bar = Double.parseDouble(barTextField.getText().replace(",", "."));
        Double price = Double.parseDouble(priceLabel.getText().replace(",", "."));
        change = bar - price;

        String back = String.format("%.2f", change);
        changeBarlabel.setText(back);
    }

}
