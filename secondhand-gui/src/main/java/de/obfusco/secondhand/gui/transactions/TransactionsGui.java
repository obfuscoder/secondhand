package de.obfusco.secondhand.gui.transactions;

import com.itextpdf.text.DocumentException;
import de.obfusco.secondhand.sale.gui.BillPDFCreator;
import de.obfusco.secondhand.storage.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class TransactionsGui extends JDialog {
    @Autowired
    TransactionsTableModel transactionsTableModel;

    private JPanel contentPane;
    private JButton printButton;
    private JButton closeButton;
    private JTable transactionTable;
    private JTable itemsTable;
    private Transaction selectedTransaction;

    @Override
    public void setVisible(boolean b) {
        if (b) {
            transactionsTableModel.init();
            transactionTable.setModel(transactionsTableModel);
        }
        super.setVisible(b);
    }

    public TransactionsGui() {
        setTitle("Transaktionen");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(printButton);
        pack();

        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        transactionTable.setTableHeader(new JTableHeader());
        transactionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                selectedTransaction = transactionsTableModel.getAt(transactionTable.getSelectedRow());
                itemsTable.setModel(new ItemTableModel(selectedTransaction.items));
            }
        });
    }

    private void onOK() {
        Path basePath = Paths.get("data/pdfs/sale");
        File pdfFile = null;
        try {
            pdfFile = new BillPDFCreator().createPdf(basePath, selectedTransaction.items);
            Desktop.getDesktop().open(pdfFile);
        } catch (IOException | DocumentException e) {
            JOptionPane.showMessageDialog(this, "Fehler",
                    "Es trat ein Fehler beim Erstellen der Rechnung auf.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
