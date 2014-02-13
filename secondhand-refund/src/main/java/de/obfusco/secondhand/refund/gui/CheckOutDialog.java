package de.obfusco.secondhand.refund.gui;

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
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckOutDialog extends JDialog implements ActionListener {

    private final static Logger LOG = LoggerFactory.getLogger(CheckOutDialog.class);

    JLabel priceLabel;
    JLabel changeBarlabel;
    JTextField barTextField;
    JFormattedTextField postCodeTextField;
    Double change;
    JLabel errorLabel;

    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    JButton printButton = new JButton("Drucken");

    JLabel title = new JLabel("Storno abschließen");

    RefundGui frame;

    ReservedItemRepository itemRepository;
    List<ReservedItem> items;

    int postCode = 0;
    private Path basePath = Paths.get("data/pdfs/refund");

    public CheckOutDialog(JFrame parentFrame) {

        super(parentFrame, "Storno abschließen", true);
        setSize(400, 300);

        frame = (RefundGui) parentFrame;
        itemRepository = frame.getItemRepository();
        this.items = frame.getTableData();
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 0, 0, 255));

        // build the whole dialog
        buildDialog();

    }

    private void buildDialog() {
        setFont(getFont().deriveFont(20f));

        setLayout(new BorderLayout());

        title.setFont(title.getFont().deriveFont(24.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(title, BorderLayout.NORTH);

        JPanel checkOutPanel = new JPanel(new GridLayout(8, 0));

        checkOutPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        JLabel sumLabel = new JLabel("Auszahlung: ");
        sumLabel.setFont(title.getFont().deriveFont(20.0f));
        priceLabel = new JLabel(frame.getPrice());
        priceLabel.setFont(title.getFont().deriveFont(20.0f));
        JLabel currencyLabel = new JLabel("Euro");
        currencyLabel.setFont(title.getFont().deriveFont(20.0f));
        JPanel sumPanel = new JPanel(new GridLayout(0, 3));
        sumPanel.add(sumLabel);
        sumPanel.add(priceLabel);
        sumPanel.add(currencyLabel);

        checkOutPanel.add(sumPanel);

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
                    completeRefund();
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

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == okButton) {
            completeRefund();
        } else if (e.getSource() == cancelButton) {
            this.dispose();
        } else if (e.getSource() == printButton) {
            try {
                File pdfFile = new RefundPDFCreator().createPdf(basePath, frame.getTableData(),
                        Double.parseDouble(frame.getPrice().replace(",", ".")));
                Desktop.getDesktop().open(pdfFile);
            } catch (DocumentException | IOException ex) {
                JOptionPane.showMessageDialog(this, "Fehler",
                        "Es trat ein Fehler beim Erstellen der Storno-Rechnung auf.",
                        JOptionPane.ERROR_MESSAGE);
                LOG.error("Error while creating refund receipt", ex);
            }
        }
    }

    private void completeRefund() {
        Date refundDate = new Date();
        for (ReservedItem item : items) {
            item.setRefunded(refundDate);
        }
        itemRepository.save(items);

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
