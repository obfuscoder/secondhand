package com.postcode.gui;

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

import com.secondhandcommon.items.reader.CsvFinder;

public class PostCode extends JFrame {

	private static final long serialVersionUID = 839062362772789004L;
	CsvFinder finder;
	JTable postCodeTable;
	PostCodeTableModel tablemodel;

	public PostCode() {
		super("PLZOverview");
		setSize(800, 800);
		setLocation(200, 50);
		addComponentsToPane(getContentPane());
		pack();
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		finder = new CsvFinder();
	}

	private void addComponentsToPane(Container pane) {
		JLabel title = new JLabel("PLZ �bersicht");
		pane.add(title, BorderLayout.NORTH);
		
		tablemodel = new PostCodeTableModel();
		
		postCodeTable = new JTable(tablemodel);
		
		pane.add(new JScrollPane(postCodeTable), BorderLayout.CENTER);
		

	}

	class PostCodeTableModel extends AbstractTableModel {

		private List<String> columnNames = new ArrayList<String>(Arrays.asList(
				"PLZ", "Anzahl"));

		private List<String[]> data = finder.getAllPostCodes();
		
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
			Object[] itemrow = (String[]) data.get(row);

			return itemrow[col];
		}
		
		@Override
		public String getColumnName(int index) {
			return (String) columnNames.get(index);
		}

	}
	
	public static void main(String args[]) {
		PostCode gui = new PostCode();
	}

}
