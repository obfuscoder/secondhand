package com.secondhandmarktgui.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.barcodefilegenerator.createFile.BarCodeGeneratorGui;
import com.payoff.gui.PayOffGui;
import com.postcode.gui.PostCode;
import com.secondhandcommon.csvread.CsvReader;
import com.secondhandsale.gui.CashBoxGui;
import com.testscan.gui.TestScanGui;

public class Secondhandmarktgui extends JFrame implements ActionListener {

	private static final long serialVersionUID = 4961295225628108431L;
	public JButton sale;
	public JButton billgenerator;
	public JButton billgeneratorComplete;
	public JButton barcodegenerator;
	public JButton plzOverview;
	public JButton testScan;
	JMenuBar menuBar;
	JMenu filemenu;
	JMenu import_export_menu;
	JMenuItem itemImportMenu;
	JMenuItem customerImportMenu;
	JMenuItem close;
	JMenuItem importMenu;
	JMenuItem exportMenu;
	JFileChooser fc;
	String customerFilepath = "C:\\flohmarkt\\Customer\\";
	String customerFile = "customer.csv";
	String completeFilepath = "C:\\flohmarkt\\completion\\";
	String soldFilepath = "C:\\flohmarkt\\Sold\\";
	String soldFile = "sold.csv";
	String completefile = "completeItemFile.csv";
	private static final String completeresult = "C:\\flohmarkt\\KomplettAbrechnung\\";
	private static final String completeresultfile = "completesold.csv";

	
	public Secondhandmarktgui() {
		super("Flohmarkt");
		setSize(800, 800);
		setLocation(200, 10);
		addComponentsToPane(getContentPane());
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == itemImportMenu) {
			int returnVal = fc.showOpenDialog(Secondhandmarktgui.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				importCSVFile(completeFilepath, completefile);
				
				CsvReader reader = new CsvReader(true);
				reader.saveItemsToCustomerFolder();
			}
		} else if (e.getSource() == customerImportMenu) {
			int returnVal = fc.showOpenDialog(Secondhandmarktgui.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				importCSVFile(customerFilepath, customerFile);
			}
		}
		else if (e.getSource() == close) {
			System.exit(0);
		} else if (e.getSource() == exportMenu) {

			ExtensionFileFilter filter = new ExtensionFileFilter(
					new String[] { ".CSV" }, "Comma Delimited File (*.CSV)");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			fc.setAcceptAllFileFilterUsed(false);
			int retrival = fc.showSaveDialog(null);
			if (retrival == fc.APPROVE_OPTION) {

				File sourceFile = new File(soldFilepath + soldFile);
				File destFile = fc.getSelectedFile();

				if (!destFile.getName().contains(".")) {
					destFile = new File(destFile.getPath() + ".csv");
				}

				if (destFile.exists()) {
					destFile.delete();
				}
				try {
					destFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				FileChannel source = null;
				FileChannel destination = null;

				try {
					source = new FileInputStream(sourceFile).getChannel();
					destination = new FileOutputStream(destFile).getChannel();
					destination.transferFrom(source, 0, source.size());

				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					try {
						if (source != null) {
							source.close();
						}
						if (destination != null) {
							destination.close();
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		} else if (e.getSource() == importMenu) {
			importMenu();
		}
	}

	private void importCSVFile(String path, String file) {
		(new File(path)).mkdirs();

		File sourceFile = fc.getSelectedFile();
		File destFile = new File(path + file);

		if (destFile.exists()) {
			destFile.delete();
		}
		try {
			destFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void importMenu() {
		File destFile = new File(completeresult + completeresultfile);
		if (!destFile.exists()) {
			(new File(completeresult)).mkdirs();
			File sourceFile = new File(soldFilepath + soldFile);

			if (destFile.exists()) {
				destFile.delete();
			}
			try {
				destFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			FileChannel source = null;
			FileChannel destination = null;

			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				try {
					if (source != null) {
						source.close();
					}
					if (destination != null) {
						destination.close();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		int returnVal = fc.showOpenDialog(Secondhandmarktgui.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			ArrayList<String> completeItems = new ArrayList<String>();
			BufferedReader completeIn;
			try {
				completeIn = new BufferedReader(new FileReader(completeresult
						+ completeresultfile));

				// einlesen
				String itemNr = "";
				while ((itemNr = completeIn.readLine()) != null)
					completeItems.add(itemNr);

				completeIn.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException ex) {

			}

			String itemNr = "";
			BufferedReader in;
			try {
				in = new BufferedReader(new FileReader(fc.getSelectedFile()));

				// einlesen
				ArrayList<String> content = new ArrayList<String>();

				while ((itemNr = in.readLine()) != null) {
					if (!completeItems.contains(itemNr)) {
						completeItems.add(itemNr);
					}
				}

				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException ex) {

			}

			Collections.sort(completeItems);
			try {
				createCompleteFile(completeItems);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void createCompleteFile(ArrayList<String> items)
			throws FileNotFoundException {
		BufferedWriter writer = null;
		try {

			writer = new BufferedWriter(new FileWriter(completeresult
					+ completeresultfile));
			for (int i = 0; i < items.size(); i++) {
				writer.write(items.get(i));
				writer.newLine();
				writer.flush();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void addComponentsToPane(Container pane) {
		JLabel title = new JLabel("Flohmarkt");
		title.setFont(title.getFont().deriveFont(50.0f));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		pane.add(title, BorderLayout.NORTH);

		fc = new JFileChooser();

		menuBar = new JMenuBar();
		filemenu = new JMenu("Datei");
		import_export_menu = new JMenu("Verkauf Import/Export");

		menuBar.add(filemenu);
		menuBar.add(import_export_menu);

		itemImportMenu = new JMenuItem("ArtikelImport");
		itemImportMenu.addActionListener(this);
		
		customerImportMenu = new JMenuItem("KundenImport");
		customerImportMenu.addActionListener(this);
		
		close = new JMenuItem("Schlie�en");
		close.addActionListener(this);

		filemenu.add(itemImportMenu);
		filemenu.add(customerImportMenu);
		filemenu.add(close);

		importMenu = new JMenuItem("Import");
		importMenu.addActionListener(this);
		exportMenu = new JMenuItem("Export");
		exportMenu.addActionListener(this);

		import_export_menu.add(importMenu);
		import_export_menu.add(exportMenu);

		this.setJMenuBar(menuBar);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 6));

		sale = new JButton("Verkauf");
		sale.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new CashBoxGui();
			}
		});

		plzOverview = new JButton("PLZ �bersicht");
		plzOverview.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new PostCode();
			}
		});

		billgenerator = new JButton("Abrechnung");
		billgenerator.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new PayOffGui(false);
			}
		});
		
		billgeneratorComplete = new JButton("Abrechnung Gesamt");
		billgeneratorComplete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new PayOffGui(true);
			}
		});

		barcodegenerator = new JButton("Barcodes drucken");
		barcodegenerator.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new BarCodeGeneratorGui();
			}
		});
		
		testScan = new JButton("Barcode- Test");
		testScan.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new TestScanGui();
			}
		});

		panel.add(sale);
		panel.add(plzOverview);
		panel.add(billgenerator);
		panel.add(billgeneratorComplete);
		panel.add(barcodegenerator);
		panel.add(testScan);

		pane.add(panel, BorderLayout.SOUTH);
	}

	public static void main(String args[]) {
		Secondhandmarktgui gui = new Secondhandmarktgui();
	}

	/**
	 * Inherited FileFilter class to facilitate reuse when multiple file filter
	 * selections are required. For example purposes, I used a static nested
	 * class, which is defined as below as a member of our original
	 * FileChooserExample class.
	 */
	static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {

		private java.util.List<String> extensions;
		private String description;

		public ExtensionFileFilter(String[] exts, String desc) {
			if (exts != null) {
				extensions = new java.util.ArrayList<String>();

				for (String ext : exts) {

					// Clean array of extensions to remove "."
					// and transform to lowercase.
					extensions.add(ext.replace(".", "").trim().toLowerCase());
				}
			} // No else need; null extensions handled below.

			// Using inline if syntax, use input from desc or use
			// a default value.
			// Wrap with an if statement to default as well as
			// avoid NullPointerException when using trim().
			description = (desc != null) ? desc.trim() : "Custom File List";
		}

		// Handles which files are allowed by filter.
		@Override
		public boolean accept(File f) {

			// Allow directories to be seen.
			if (f.isDirectory())
				return true;

			// exit if no extensions exist.
			if (extensions == null)
				return false;

			// Allows files with extensions specified to be seen.
			for (String ext : extensions) {
				if (f.getName().toLowerCase().endsWith("." + ext))
					return true;
			}

			// Otherwise file is not shown.
			return false;
		}

		// 'Files of Type' description
		@Override
		public String getDescription() {
			return description;
		}
	}
}
