package de.obfusco.secondhand.refund.gui;

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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CommitRefundDialog extends JDialog implements ActionListener {

    private final static Logger LOG = LoggerFactory.getLogger(CommitRefundDialog.class);

    private JLabel errorLabel;

    private JButton okButton = new JButton("OK");
    private JButton cancelButton = new JButton("Cancel");
    private JButton printButton = new JButton("Drucken");

    private JLabel title = new JLabel("Storno abschließen");

    private RefundGui refundGui;

    private StorageService storageService;
    private List<BaseItem> items;

    private Path basePath = Paths.get("data/pdfs/refund");
    private TransactionListener transactionListener;

    CommitRefundDialog(JFrame parentFrame, TransactionListener transactionListener) {

        super(parentFrame, "Storno abschließen", true);
        setSize(400, 300);

        refundGui = (RefundGui) parentFrame;
        this.storageService = refundGui.getStorageService();
        this.transactionListener = transactionListener;
        this.items = refundGui.getTableData();
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 0, 0, 255));

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

        JLabel sumLabel = new JLabel("Summe:");
        sumLabel.setFont(title.getFont().deriveFont(20.0f));
        JLabel priceLabel = new JLabel(refundGui.getPrice());
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
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    completeRefund();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                    refundGui.getItemNr().requestFocus();
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
                File pdfFile = new RefundPDFCreator().createPdf(basePath, refundGui.getTableData(),
                        Double.parseDouble(refundGui.getPrice().replace(",", ".")));
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
        List<String> itemCodes = items.stream().map(it -> it.code).collect(Collectors.toList());
        Transaction transaction = storageService.storeRefundInformation(itemCodes);
        transactionListener.notify(transaction);

        refundGui.getReadyButton().setEnabled(false);
        refundGui.getItemNr().setEnabled(false);
        refundGui.getCashTable().setEnabled(false);

        this.dispose();
    }
}
