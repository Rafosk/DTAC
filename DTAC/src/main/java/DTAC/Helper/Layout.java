package DTAC.Helper;

import java.awt.EventQueue;

import javax.swing.JFrame;

import net.lingala.zip4j.exception.ZipException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
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
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		PrintWriter writer = new PrintWriter("main.log", "UTF-8");
		Files.write(Paths.get("main.log"), "START\n".getBytes(), StandardOpenOption.APPEND);				
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
		Files.write(Paths.get("main.log"), "STOP\n".getBytes(), StandardOpenOption.APPEND);
		writer.close();
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
		JTextPane txtpnStart = new JTextPane();
		txtpnStart.setText("ready");
		txtpnDs.setEditable(false);
		txtpnDs.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtpnDs.setBackground(UIManager.getColor("Button.background"));
		StyledDocument doc = txtpnDs.getStyledDocument();
		btnDtac.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				Runnable counter = () -> {
					try {
						for (int i = 0; i < 100; i++) {
							TimeUnit.SECONDS.sleep(1);
							String timeStamp = new SimpleDateFormat("mmss").format(Calendar.getInstance().getTime());
							txtpnStart.setText(timeStamp);	
						}
						
					} catch (Exception e2) {
						txtpnStart.setText(e2.getMessage());
					}									
				};
				Thread thread2 = new Thread(counter);
				thread2.start();
				
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
					.addGap(18)
					.addComponent(txtpnDs, GroupLayout.PREFERRED_SIZE, 556, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(46, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(277)
					.addComponent(btnDtac)
					.addPreferredGap(ComponentPlacement.RELATED, 173, Short.MAX_VALUE)
					.addComponent(txtpnStart, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
					.addGap(68))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(57)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(btnDtac)
						.addComponent(txtpnStart, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
					.addGap(41)
					.addComponent(txtpnDs, GroupLayout.PREFERRED_SIZE, 409, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(30, Short.MAX_VALUE))
		);
		frame.getContentPane().setLayout(groupLayout);
	}
}
