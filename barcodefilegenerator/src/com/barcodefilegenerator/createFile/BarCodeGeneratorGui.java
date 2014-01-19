package com.barcodefilegenerator.createFile;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.itextpdf.text.DocumentException;

public class BarCodeGeneratorGui extends JFrame implements ActionListener {

	public JButton justBarcode;
	public JButton etiquettes;
	public JLabel customerNrLabel;
	public JTextField customerNr;

	public JLabel etiquettesLabel;
	public JLabel etiquettesLink;
	public String filename;

	public BarCodeGeneratorGui() {
		super("Etiketten/ Barcodes");
		setSize(1000, 800);
		setLocation(200, 50);
		addComponentsToPane(getContentPane());
		pack();
		setVisible(true);
	}

	private void addComponentsToPane(Container pane) {
		JLabel title = new JLabel("Etiketten/ Barcodes");
		title.setFont(title.getFont().deriveFont(30.0f));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		pane.add(title, BorderLayout.NORTH);

		JPanel panel = new JPanel();

		customerNrLabel = new JLabel("Kundennr:");
		customerNr = new JTextField();

		justBarcode = new JButton("Nur BarCodes");
		justBarcode.addActionListener(this);
		etiquettes = new JButton("Etiketten erzeugen");
		etiquettes.addActionListener(this);

		etiquettesLabel = new JLabel();
		etiquettesLink = new JLabel();
		etiquettesLink
				.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		etiquettesLink.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().open(new File(filename));
				} catch (IOException e1) {

					e1.printStackTrace();
				}
			}
		});

		panel.setLayout(new GridLayout(3, 2));
		panel.add(customerNrLabel);
		panel.add(customerNr);
		panel.add(justBarcode);
		panel.add(etiquettes);
		panel.add(etiquettesLabel);
		panel.add(etiquettesLink);

		pane.add(panel, BorderLayout.CENTER);

	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == justBarcode) {

			try {

				filename = new BARCodeSheet().createPDFFile(Integer
						.parseInt(customerNr.getText()));
				
				etiquettesLabel.setText("Barcodes für " + customerNr.getText());
				
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (event.getSource() == etiquettes) {
			try {

				filename = new BarCodeLabelSheet().createPDFFile(Integer
						.parseInt(customerNr.getText()));
				
				etiquettesLabel.setText("Etiketten für " + customerNr.getText());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		etiquettesLink.setText(filename);
	}
}
