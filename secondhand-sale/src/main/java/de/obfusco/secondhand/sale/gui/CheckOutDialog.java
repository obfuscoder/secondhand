package de.obfusco.secondhand.sale.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class CheckOutDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -9004809235134991240L;

	JLabel errorLabel;
	JLabel priceLabel;
	JTable cashTable;
	JLabel changeBarlabel;
	JTextField barTextField;
	JFormattedTextField postCodeTextField;
	String sum;
	String change;
	
	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");

	JLabel title = new JLabel("Verkauf abschließen");

	public CheckOutDialog(JFrame parentFrame, String sum) {

		super(parentFrame, "Verkauf abschließen", true);
		setSize(400, 250);
		// build the whole dialog
		buildNewObjectDialog(sum);

	}

	private void buildNewObjectDialog(String sum) {

		setLayout(new BorderLayout());

		title.setFont(title.getFont().deriveFont(24.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(title, BorderLayout.NORTH);

		JPanel checkOutPanel = new JPanel(new GridLayout(8, 0));

		checkOutPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		
		JLabel sumLabel = new JLabel("SUMME: ");
		priceLabel = new JLabel(sum);
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
				}

			}

		});

		postCodeTextField = new JFormattedTextField();
		postCodeTextField.setColumns(5);

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

		JPanel buttonPanel = new JPanel(new GridLayout(0, 2));

		
		
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		this.add(buttonPanel, BorderLayout.SOUTH);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if( e.getSource() == okButton )
		{
			
		}
		else
		{
			
		}

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

}
