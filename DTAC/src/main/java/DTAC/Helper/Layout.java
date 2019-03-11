package DTAC.Helper;

import java.awt.EventQueue;

import javax.swing.JFrame;

import net.lingala.zip4j.exception.ZipException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JTextPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.Font;

public class Layout {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Layout window = new Layout();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Layout() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 626, 589);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JButton btnDtac = new JButton("FIX");
		JTextPane txtpnDs = new JTextPane();
		txtpnDs.setEditable(false);
		txtpnDs.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtpnDs.setBackground(UIManager.getColor("Button.background"));
		StyledDocument doc = txtpnDs.getStyledDocument();
		btnDtac.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Dtac dtac = new Dtac();
					
				Runnable task = () -> {
					try {
						dtac.repair(doc, btnDtac);
					} catch (IOException | ZipException | BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
				};
				Thread thread = new Thread(task);
				thread.start();

			}
		});
		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addComponent(txtpnDs, GroupLayout.PREFERRED_SIZE, 556, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(277)
							.addComponent(btnDtac)))
					.addContainerGap(46, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(57)
					.addComponent(btnDtac)
					.addGap(41)
					.addComponent(txtpnDs, GroupLayout.PREFERRED_SIZE, 409, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(30, Short.MAX_VALUE))
		);
		frame.getContentPane().setLayout(groupLayout);
	}
}
