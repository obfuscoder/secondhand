package de.obfusco.secondhand.sale.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.sale.service.StorageService;

public class CheckOutDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -9004809235134991240L;

	JLabel priceLabel;
	JLabel changeBarlabel;
	JTextField barTextField;
	JFormattedTextField postCodeTextField;
	String change;
	JLabel errorLabel;

	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");
	JButton printButton = new JButton("Drucken");

	JLabel title = new JLabel("Verkauf abschließen");

	CashBoxGui frame;

	StorageService storageService;
	List<String> items;

	int postCode = 0;

	private static final String PRINTTO = "C:\\flohmarkt\\bill.pdf";
	private static final String PRINTTOIMG = "C:\\flohmarkt\\billimage";

	public CheckOutDialog(JFrame parentFrame) {

		super(parentFrame, "Verkauf abschließen", true);
		setSize(400, 300);

		frame = (CashBoxGui) parentFrame;
		this.storageService = frame.getStorageService();
		this.items = frame.getTableItems();
		errorLabel = new JLabel(" ");
		errorLabel.setForeground(new Color(255, 0, 0, 255));

		// build the whole dialog
		buildNewObjectDialog();

	}

	private void buildNewObjectDialog() {

		setLayout(new BorderLayout());

		title.setFont(title.getFont().deriveFont(24.0f));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(title, BorderLayout.NORTH);

		JPanel checkOutPanel = new JPanel(new GridLayout(8, 0));

		checkOutPanel.add(new JSeparator(JSeparator.HORIZONTAL));

		JLabel sumLabel = new JLabel("SUMME: ");
		priceLabel = new JLabel(frame.getPrice());
		JLabel sumeuroLabel = new JLabel("Euro");
		JPanel sumPanel = new JPanel(new GridLayout(0, 3));
		sumLabel.setFont(title.getFont().deriveFont(20.0f));
		priceLabel.setFont(title.getFont().deriveFont(20.0f));
		sumeuroLabel.setFont(title.getFont().deriveFont(20.0f));
		sumPanel.add(sumLabel);
		sumPanel.add(priceLabel);
		sumPanel.add(sumeuroLabel);

		JLabel bareuroLabel = new JLabel("Euro");
		JLabel barLabel = new JLabel("BAR (optinal)");
		barTextField = new JTextField();
		barTextField.addKeyListener(new KeyListener() {

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

					calculateChange();
					postCodeTextField.requestFocus();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.getItemNr().requestFocus();
					dispose();
				}

			}

		});

		postCodeTextField = new JFormattedTextField();
		postCodeTextField.setColumns(5);
		postCodeTextField.addKeyListener(new KeyListener() {

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

					getPostcode();
					okButton.requestFocus();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.getItemNr().requestFocus();
					dispose();
				}

			}

		});

		JLabel changeeuroLabel = new JLabel("Euro");
		JLabel changeLabel = new JLabel("RÜCKGELD");
		changeBarlabel = new JLabel("0,00");
		changeBarlabel.setForeground(Color.red);

		JLabel postCodeLabel = new JLabel("PLZ (optional)");

		JPanel barPanel = new JPanel(new GridLayout(0, 3));
		barPanel.add(barLabel);
		barPanel.add(barTextField);
		barPanel.add(bareuroLabel);

		JPanel changePanel = new JPanel(new GridLayout(0, 3));
		changePanel.add(changeLabel);
		changePanel.add(changeBarlabel);
		changePanel.add(changeeuroLabel);

		JPanel postCodePanel = new JPanel(new GridLayout(0, 3));
		postCodePanel.add(postCodeLabel);
		postCodePanel.add(postCodeTextField);

		checkOutPanel.add(sumPanel);
		checkOutPanel.add(barPanel);
		checkOutPanel.add(changePanel);
		checkOutPanel.add(new JPanel());
		checkOutPanel.add(postCodePanel);
		checkOutPanel.add(new JPanel());

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
					completeOrder();
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

	private boolean getPostcode() {
		String postText = postCodeTextField.getText();
		postCode = 0;
		if (postText.length() == 5) {
			try {
				postCode = Integer.parseInt(postText);
				errorLabel.setText("");
				return true;
			} catch (NumberFormatException ex) {
				errorLabel.setText("Ungültige PLZ. Bitte nur Zahlen eingeben.");
				postCodeTextField.requestFocus();
				return false;
			}
		} else if (postText.length() != 0) {
			errorLabel.setText("Ungültige PLZ. 5 Stellen bitte.");
			postCodeTextField.requestFocus();
			return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == okButton) {
			completeOrder();
		} else if (e.getSource() == cancelButton) {
			this.dispose();
		} else if (e.getSource() == printButton) {
			try {
				String bar = "";
				if (getBarString() != null && !getBarString().equals("")) {
					bar = String.format("%.2f",
							Float.parseFloat(getBarString()));
				} else {
					bar = String.format("%.2f", Float.parseFloat(frame
							.getPrice().replace(",", ".")));
				}
				new BillPDFCreator().createPdf(PRINTTO, frame.getTableData(),
						frame.getPrice(), bar, getChange());
			} catch (IOException | DocumentException ex) {
				ex.printStackTrace();
			}

			PDDocument document = null;
			try {
				document = PDDocument.load(PRINTTO);
				int pages = document.getPageCount();
				PDFImageWriter writer = new PDFImageWriter();
				boolean success = writer.writeImage(document, "jpg", "", 1, 1,
						PRINTTOIMG, BufferedImage.TYPE_INT_RGB, Toolkit
								.getDefaultToolkit().getScreenResolution());

				DocFlavor flavor = DocFlavor.INPUT_STREAM.JPEG;
				PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
				PrintService pservice = PrintServiceLookup
						.lookupDefaultPrintService();
				DocPrintJob pj = pservice.createPrintJob();
				for (int i = 0; i < pages; i++) {
					try {
						FileInputStream fis = new FileInputStream(PRINTTOIMG
								+ (i + 1) + ".jpg");
						Doc doc = new SimpleDoc(fis, flavor, null);
						pj.print(doc, aset);
					} catch (FileNotFoundException fe) {
					} catch (PrintException ex) {
						System.out.println("Fehler beim drucken: " + ex);
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	private void completeOrder() {

		calculateChange();

		boolean postcodeOK = true;
		if (postCode == 0 && postCodeTextField.getText().length() > 0) {
			postcodeOK = getPostcode();
		}
		if (!postcodeOK)
			return;
		storageService.storeSoldInformation(items, postCode);

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
		float bar = Float
				.parseFloat((barTextField.getText()).replace(",", "."));
		float prise = Float
				.parseFloat((priceLabel.getText()).replace(",", "."));
		float back = bar * 100 - prise * 100;
		back /= 100;

		change = String.format("%.2f", back);
		changeBarlabel.setText(change);
	}

	public String getBarString() {
		return barTextField.getText();
	}

	public String getChange() {
		return change;
	}

}
