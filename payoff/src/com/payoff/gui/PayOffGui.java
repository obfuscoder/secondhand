package com.payoff.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.parser.XObjectDoHandler;
import com.payoff.file.PayOffFile;
import com.secondhandcommon.items.reader.CsvFinder;

public class PayOffGui extends JFrame {

	public JLabel totalPayoff;
	public JLabel totalPayoffLink;

	private String resultpath = "C:\\flohmarkt\\Abrechnung\\";
	private String completeresultpath = "C:\\flohmarkt\\KomplettAbrechnung\\";
	private static final String filename = "total_payoff.pdf";

	private String payofftitle = "Abrechnung";
	private String completepayofftitle = "Abrechnung Gesamt";
	private static String titel = "Abrechnung";
	
	CsvFinder finder;

	Map customerMap;

	public PayOffGui(boolean completePayoff) {
		super(titel);

		finder = new CsvFinder();

		customerMap = finder.getAllCustomer();

		if (completePayoff) {
			payofftitle = completepayofftitle;
			resultpath = completeresultpath;
		}

		setSize(1000, 800);
		setLocation(200, 50);
		new PayOffFile(completePayoff);
		addComponentsToPane(getContentPane());
		pack();
		setVisible(true);
	}

	private void addComponentsToPane(Container pane) {
		JLabel title = new JLabel(payofftitle);
		title.setFont(title.getFont().deriveFont(30.0f));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		pane.add(title, BorderLayout.NORTH);

		JPanel panel = new JPanel();

		totalPayoff = new JLabel("Gesamtübersicht");
		totalPayoff.setFont(title.getFont().deriveFont(20.0f));
		totalPayoffLink = new JLabel(resultpath + filename);
		totalPayoffLink.setCursor(Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		totalPayoffLink.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().open(new File(resultpath + filename));
				} catch (IOException e1) {

					e1.printStackTrace();
				}
			}
		});

		// SubDirs
		File file = new File(resultpath);
		String[] names = file.list();

		panel.setLayout(new GridLayout(names.length+2, 2));
		panel.setBorder(new EmptyBorder(12, 12, 12, 12));
		
		panel.add(new JSeparator(JSeparator.HORIZONTAL));
		panel.add(new JSeparator(JSeparator.HORIZONTAL));

		panel.add(totalPayoff);
		panel.add(totalPayoffLink);
		
		panel.add(new JSeparator(JSeparator.HORIZONTAL));
		panel.add(new JSeparator(JSeparator.HORIZONTAL));
		
		
		for (String name : names) {
			if (new File(resultpath + name).isDirectory()) {

				if (!customerMap.containsKey(Integer.parseInt(name))) {
					return;
				}

				Object[] customer = (Object[]) customerMap.get(Integer.parseInt(name));
				JLabel customerPayoffNr = new JLabel(name + " | " + (String) customer[1]);
				
				customerPayoffNr.setFont(title.getFont().deriveFont(14.0f));
				final String file_name = resultpath + name + "\\" + filename;
				JLabel payoffLink = new JLabel(file_name);

				payoffLink.addMouseListener(new MouseListener() {

					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().open(new File(file_name));
						} catch (IOException e1) {

							e1.printStackTrace();
						}
					}
				});
				panel.add(customerPayoffNr);
				panel.add(payoffLink);
			}
		}

		pane.add(panel, BorderLayout.CENTER);

	}
}
