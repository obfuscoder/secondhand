package de.obfusco.secondhand.postcode.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.obfusco.secondhand.storage.model.ZipCodeCount;
import de.obfusco.secondhand.storage.repository.CustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostCodeGui extends JFrame {

    private static final long serialVersionUID = 839062362772789004L;
    JTable postCodeTable;
    PostCodeTableModel tablemodel;

    CustomerRepository customerRepository;

    @Autowired
    public PostCodeGui(CustomerRepository customerRepository) {
        super("PLZ Übersicht");
        this.customerRepository = customerRepository;
        setSize(800, 800);
        setLocation(200, 50);
        addComponentsToPane(getContentPane());
        pack();
    }

    private void addComponentsToPane(Container pane) {
        JLabel title = new JLabel("PLZ Übersicht");
        pane.add(title, BorderLayout.NORTH);
        tablemodel = new PostCodeTableModel(customerRepository.getZipCodeCounts());

        postCodeTable = new JTable(tablemodel);

        pane.add(new JScrollPane(postCodeTable), BorderLayout.CENTER);

    }

    class PostCodeTableModel extends AbstractTableModel {

        private List<String> columnNames = new ArrayList<>(Arrays.asList(
                "PLZ", "Anzahl"));
        private List<ZipCodeCount> zipCodeCounts;

        private PostCodeTableModel(List<ZipCodeCount> zipCodeCounts) {
            this.zipCodeCounts = zipCodeCounts;
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public int getRowCount() {
            return zipCodeCounts.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return zipCodeCounts.get(row).getZipCode();
                case 1:
                    return zipCodeCounts.get(row).getCount();
            }
            return null;
        }

        @Override
        public String getColumnName(int index) {
            return (String) columnNames.get(index);
        }
    }
}
