package com.testscan.gui;

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

import com.secondhandcommon.items.reader.CsvFinder;
import com.secondhandcommon.sold.file.SoldFileWriter;

public class TestScanGui extends JFrame implements ActionListener {

	private static final long serialVersionUID = -698049510249510666L;
	CsvFinder finder;
	JTextField itemNr;
	CashTableModel tablemodel;
	JLabel errorLabel;
	JTable cashTable;

	SoldFileWriter nrToFileWriter;

	JButton clearButton = new JButton("Tabelle leeren");

	public TestScanGui() {
		super("Barcode Test");
		setSize(600, 600);
		setLocation(400, 10);
		addComponentsToPane(getContentPane());
		pack();
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		finder = new CsvFinder();
		nrToFileWriter = new SoldFileWriter();
	}

	private void addComponentsToPane(Container pane) {

		JLabel title = new JLabel("Barcode Test");
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

					// int checksum = 0;
					// for (int i = 0; i < 7; i++) {
					// checksum += Character.getNumericValue(itemText
					// .charAt(i));
					// }
					//
					// if (Character.getNumericValue(itemText.charAt(7)) !=
					// checksum % 10) {
					// setErrorText("Artikelnummer " + itemNr.getText() +
					// " ist falsch! Bitte überprüfen Sie die Eingabe.");
					// itemNr.setText("");
					// return;
					// }

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

		pane.add(itemPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout(0, 3));

		buttonPanel.add(clearButton);

		clearButton.addActionListener(this);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout());
		southPanel.add(buttonPanel);

		pane.add(southPanel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == clearButton) {

			itemNr.setText("");
			int rowCount = tablemodel.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tablemodel.delRow(0);
			}
			itemNr.requestFocus();
		} 

	}

	class CashTableModel extends AbstractTableModel {
		private List<String> columnNames = new ArrayList<String>(Arrays.asList(
				"ArtNr", "Kategorie", "Bezeichnung", "Groesse", "Preis"));

		private List<Object> data = new ArrayList<Object>();

		public List<Object> getData() {
			return data;
		}

		public List<Object> getColumnData(int col) {
			List<Object> columnData = new ArrayList<Object>();
			for (int i = 0; i < data.size(); i++) {
				Object[] row = (Object[]) data.get(i);
				columnData.add(row[col]);
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
				this.fireTableDataChanged();
			}
		}

		public void delRow(int row) {

			Object[] itemrow = (Object[]) data.get(row);
			data.remove(row);
			this.fireTableDataChanged();

		}

	}

	public void addItem() {
		setErrorText(" ");
		Object[] data;
		data = finder.getItemForNr(itemNr.getText());
		if (data != null) {
			tablemodel.addRow(data);
		} else {
			setErrorText("Artikel mit Nummer \"" + itemNr.getText()
					+ "\" existiert nicht!");
		}

		itemNr.setText("");
	}

	public void setErrorText(String text) {
		errorLabel.setText(text);
		errorLabel.getParent().invalidate();
		errorLabel.getParent().validate();
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

	public static void main(String args[]) {
		TestScanGui gui = new TestScanGui();
	}

}
