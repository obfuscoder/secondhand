package de.obfusco.secondhand.sale.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.obfusco.secondhand.sale.service.StorageService;
import de.obfusco.secondhand.storage.model.ReservedItem;

@Component
public class CashBoxGui extends JFrame implements ActionListener {

	private static final long serialVersionUID = -698049510249510666L;
	JTextField itemNr;
	CashTableModel tablemodel;
	JLabel errorLabel;
	JLabel priceLabel;
	JTable cashTable;
	String sum;

	@Autowired
	StorageService storageService;

	JButton readyButton = new JButton("Fertig");
	JButton newButton = new JButton("Neuer Kunde");

	CheckOutDialog checkout = null;

	public CashBoxGui() {
		super("Flohmarkt Verkauf");
		setSize(600, 800);
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
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
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
		cashTable.getColumnModel().getColumn(2).setPreferredWidth(300);

		itemNr = new JTextField();
		itemNr.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String itemText = itemNr.getText();
					if (itemText.length() == 0
							&& cashTable.getModel().getRowCount() > 0) {
						itemNr.setText("");
						openDialog();
						return;
					}
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

		newButton.setEnabled(false);
		JPanel buttonPanel = new JPanel(new GridLayout(0, 2));

		buttonPanel.add(readyButton);
		buttonPanel.add(newButton);

		readyButton.addActionListener(this);
		readyButton.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					openDialog();
				}
			}
		});
		newButton.addActionListener(this);
		newButton.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					newCustomer();
				}
				
			}
		});

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(1, 0));
		southPanel.add(buttonPanel);

		pane.add(southPanel, BorderLayout.SOUTH);
	}

	public StorageService getStorageService() {
		return storageService;
	}

	public List<String> getTableItems() {
		return tablemodel.getColumnData(0);
	}
	
	public List<Object> getTableData() {
		return tablemodel.getData();
	}

	public String getPrice() {
		return priceLabel.getText();
	}

	public JButton getNewButton() {
		return newButton;
	}

	public JButton getReadyButton() {
		return readyButton;
	}

	public JTextField getItemNr() {
		return itemNr;
	}

	public JTable getCashTable() {
		return cashTable;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == readyButton) {

			openDialog();

		} else if (event.getSource() == newButton) {

			newCustomer();
		} 
	}

	private void newCustomer() {
		checkout = null;
		newButton.setEnabled(false);
		readyButton.setEnabled(true);
		itemNr.setEnabled(true);
		cashTable.setEnabled(true);

		itemNr.setText("");
		int rowCount = tablemodel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tablemodel.delRow(0);
		}

		priceLabel.setText("0,00");
		validate();
		pack();

		itemNr.requestFocus();
	}

	private void openDialog() {
		errorLabel.setText("");

		checkout = new CheckOutDialog(this);
		checkout.setLocationRelativeTo(this);
		checkout.setVisible(true);
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
			setErrorText("Artikel mit Nummer \"" + code + "\" existiert nicht!");
			return;
		}
		if (reservedItem.isSold()) {
			setErrorText("Artikel mit Nummer \"" + code
					+ "\" wurde bereits verkauft!");
			return;
		}
		data = new Object[] { reservedItem.getCode(),
				reservedItem.getItem().getCategory().getName(),
				reservedItem.getItem().getDescription(),
				reservedItem.getItem().getSize(),
				reservedItem.getItem().getPrice().toString() };
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
