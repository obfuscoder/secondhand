package de.obfusco.secondhand.sale.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.sale.service.StorageService;

public class CheckOutDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -9004809235134991240L;

    JLabel priceLabel;
    JLabel changeBarlabel;
    JTextField barTextField;
    JFormattedTextField postCodeTextField;
    Double change;
    JLabel errorLabel;

    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    JButton printButton = new JButton("Drucken");

    JLabel title = new JLabel("Verkauf abschließen");

    CashBoxGui frame;

    StorageService storageService;
    List<String> items;

    int postCode = 0;
    private Path basePath = Paths.get("data/pdfs/sale");

    public CheckOutDialog(JFrame parentFrame) {

        super(parentFrame, "Verkauf abschließen", true);
        setSize(400, 300);

        frame = (CashBoxGui) parentFrame;
        this.storageService = frame.getStorageService();
        this.items = frame.getTableItems();
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 0, 0, 255));

        // build the whole dialog
        buildNewObjectDialog();

    }

    private void buildNewObjectDialog() {

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
                    postCodeTextField.requestFocus();
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
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

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

        checkOutPanel.add(sumPanel);
        checkOutPanel.add(barPanel);
        checkOutPanel.add(changePanel);
        checkOutPanel.add(new JPanel());
        checkOutPanel.add(postCodePanel);
        checkOutPanel.add(new JPanel());

        this.add(checkOutPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));

        bottomPanel.add(errorLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 3));

        okButton.addActionListener(this);
        okButton.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub

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
        postCode = 0;
        if (postText.length() == 5) {
            try {
                postCode = Integer.parseInt(postText);
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
                Double bar;
                if (getBarString() != null && !getBarString().equals("")) {
                    bar = Double.parseDouble(getBarString());
                } else {
                    bar = Double.parseDouble(frame.getPrice().replace(",", "."));
                }
                File pdfFile = new BillPDFCreator().createPdf(basePath, frame.getTableData(),
                        Double.parseDouble(frame.getPrice()), bar, getChange());
                Desktop.getDesktop().open(pdfFile);
            } catch (DocumentException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void completeOrder() {

        calculateChange();

        boolean postcodeOK = true;
        if (postCode == 0 && postCodeTextField.getText().length() > 0) {
            postcodeOK = getPostcode();
        }
        if (!postcodeOK) {
            return;
        }
        storageService.storeSoldInformation(items, postCode);

        frame.getNewButton().setEnabled(true);
        frame.getReadyButton().setEnabled(false);
        frame.getItemNr().setEnabled(false);
        frame.getCashTable().setEnabled(false);

        this.dispose();
    }

    private void calculateChange() {

        if (barTextField.getText() == null || barTextField.getText().equals("")) {
            return;
        }
        Double bar = Double.parseDouble(barTextField.getText().replace(",", "."));
        Double price = Double.parseDouble(priceLabel.getText().replace(",", "."));
        change = bar - price;
        if (change == null) {
            change = 0.0;
        }

        String back = String.format("%.2f", change);
        changeBarlabel.setText(back);
    }

    public String getBarString() {
        return barTextField.getText();
    }

    public Double getChange() {
        return change;
    }

}
