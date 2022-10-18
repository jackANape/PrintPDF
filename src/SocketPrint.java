import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Font;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Window.Type;
import java.awt.SystemColor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.border.LineBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SocketPrint extends JFrame {

	public static SocketPrint frame;
	private JPanel contentPane;
	public static JTextField printerField;
	public static JTextField ipField;
	public static String weeklyDir = "//10.22.5.1/Docs/pdf/Weekly/";
	public static String f1Dir = "//10.22.5.1/Docs/pdf/F1/";
	public static String f2Dir = "//10.22.5.1/Docs/pdf/F2/";
	public static String printerIP = "10.22.5.241"; //office2 - 10.22.5.240, logistics - 10.22.5.242
	public static String[][] printerList = {{"10.22.5.240", "Office 2"},
			{"10.22.5.242", "Logistics"},
			{"10.22.5.241", "Office 1"},
			{"10.22.5.251", "Picking Slip"}};
	public static JTextField copiesAmount;
	public static JCheckBox staplesCheck;
	public static JCheckBox duplexCheck;
	private static boolean isMaximum = false;
	public static JTextArea logArea;
	public static JScrollPane scrollPane;
	public static JButton f1Btn;
	public static JButton f2Btn;	
	public static JButton weeklyBtn;
	public static Thread thread;
	public static JLabel versionLbl;
	public static String version = "1.0.0.1";


	public static void begin(JButton button, String source) throws InterruptedException
	{
		thread = new Thread() {
			public void run() {
				button.setEnabled(false);
				frame.setTitle("Printing..");

				PrintDir(source);

				button.setEnabled(true);
				frame.setTitle("Printing Complete!");
			}
		};

		thread.start();

	}

	public static void PrintDir(String source)
	{

		System.out.println("Printing " + source);
		appendLog("Print Starting.. ");
		String[] dir;
		File listDir = new File(source);

		dir = listDir.list();

		for(String file : dir)
		{
			System.out.println("IP Address: " + printerIP);
			System.out.println("Printing.. " + source + file);
			print(new File(source + file), printerIP);
		}

		System.out.println("Finished Printing!");
		appendLog("Print Finished.");
	}

	// this works, it also printed faster than javax.print when tested
	private static void print(File document, String printerIpAddress)
	{
		try (Socket socket = new Socket(printerIpAddress, 9100))
		{
			appendLog("Printing.. " + document);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			byte[] bytes = Files.readAllBytes(document.toPath());

			out.write(27); //esc
			out.write("%-12345X@PJL\n".getBytes());

			String copies = "@PJL SET QTY=" + copiesAmount.getText() + "\n";
			out.write(copies.getBytes());

			if(duplexCheck.isSelected())
			{
				out.write("@PJL SET DUPLEX=ON\n".getBytes());
			}

			out.write("@PJL SET FINISH=STAPLE\n".getBytes());

			if(staplesCheck.isSelected()) 
			{
				out.write("@PJL SET STAPLE=LEFTTOP\n".getBytes());
			}

			out.write("@PJL ENTER LANGUAGE=PDF\n".getBytes());
			out.write(bytes);
			out.write(27); //esc
			out.write("%-12345X".getBytes());
			out.flush();
			out.close();

		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null,"Error has occurred!","Alert",JOptionPane.WARNING_MESSAGE);
			System.out.println("Printer Not Found!" + e);
		}
	}

	public static void changePrinterName(String ip)
	{
		boolean isFound = false;

		for(int i = 0; i < printerList.length; i++)
		{
			if(ip.equalsIgnoreCase(printerList[i][0]))
			{
				printerField.setText(printerList[i][1]);
				isFound=true;
			}
		}

		if(!isFound)
		{
			printerField.setText("Unknown");
		}
	}

	public static void appendLog(String message)
	{
		logArea.append("-> " + message + "\n");

		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
		
		
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new SocketPrint();
					frame.setVisible(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null,"Error has occurred!","Alert",JOptionPane.WARNING_MESSAGE);
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SocketPrint() {
		setTitle("PrinterPDF");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 385, 450);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setLayout(null);

		JLabel lblDuplex = new JLabel("Duplex:");
		lblDuplex.setBounds(17, 41, 56, 17);
		panel.add(lblDuplex);
		lblDuplex.setFont(new Font("Tahoma", Font.PLAIN, 14));

		duplexCheck = new JCheckBox("");
		duplexCheck.setBounds(73, 40, 21, 21);
		panel.add(duplexCheck);
		duplexCheck.setFont(new Font("Dialog", Font.PLAIN, 13));

		JLabel lblNewLabel = new JLabel("Printer IP:");
		lblNewLabel.setBounds(17, 15, 61, 17);
		panel.add(lblNewLabel);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));

		ipField = new JTextField();
		ipField.setBounds(90, 12, 114, 23);
		panel.add(ipField);
		ipField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printerIP = ipField.getText();
				changePrinterName(printerIP);
				System.out.println("New IP: " + printerIP);
			}
		});

		ipField.setText("10.22.5.241");

		ipField.setFont(new Font("Dialog", Font.PLAIN, 14));
		ipField.setColumns(10);

		printerField = new JTextField();
		printerField.setBounds(210, 13, 124, 21);
		panel.add(printerField);
		printerField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		printerField.setEditable(false);
		printerField.setText("Office 1");
		printerField.setColumns(10);

		f1Btn = new JButton("Fortnight 1");
		f1Btn.setBounds(12, 92, 156, 35);
		panel.add(f1Btn);

		JLabel lblStaples = new JLabel("Staples:");
		lblStaples.setBounds(127, 42, 56, 17);
		panel.add(lblStaples);
		lblStaples.setFont(new Font("Tahoma", Font.PLAIN, 14));

		staplesCheck = new JCheckBox("");
		staplesCheck.setBounds(183, 41, 21, 21);
		panel.add(staplesCheck);
		staplesCheck.setSelected(true);
		staplesCheck.setFont(new Font("Dialog", Font.PLAIN, 13));

		JLabel lblCopies = new JLabel("Copies:");
		lblCopies.setBounds(237, 42, 56, 17);
		panel.add(lblCopies);
		lblCopies.setFont(new Font("Tahoma", Font.PLAIN, 14));

		copiesAmount = new JTextField();
		copiesAmount.setBounds(296, 41, 38, 20);
		panel.add(copiesAmount);
		copiesAmount.setHorizontalAlignment(SwingConstants.CENTER);
		copiesAmount.setText("1");
		copiesAmount.setFont(new Font("Tahoma", Font.PLAIN, 12));
		copiesAmount.setColumns(10);

		f2Btn = new JButton("Fortnight 2");
		f2Btn.setBounds(178, 92, 156, 35);
		panel.add(f2Btn);

		weeklyBtn = new JButton("Weekly");
		weeklyBtn.setBounds(12, 139, 322, 54);
		panel.add(weeklyBtn);

		JButton btnNewButton_1 = new JButton("Expand");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(isMaximum)
				{
					setBounds(100, 100, 385, 450);
					isMaximum = false;
				}
				else
				{
					setBounds(100, 100, 385, 600);
					isMaximum = true;
				}
			}
		});

		scrollPane = new JScrollPane();

		logArea = new JTextArea();
		logArea.setWrapStyleWord(true);
		logArea.setLineWrap(true);
		logArea.setEditable(false);
		scrollPane.setViewportView(logArea);
		
		versionLbl = new JLabel(version);
		versionLbl.setHorizontalAlignment(SwingConstants.CENTER);
		versionLbl.setFont(new Font("Tahoma", Font.ITALIC, 9));
		
		JButton btnNewButton = new JButton("New button");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(5)
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 348, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(145)
							.addComponent(btnNewButton_1, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(303)
							.addComponent(versionLbl, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(5)
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 348, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(6)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(19)
							.addComponent(btnNewButton_1))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnNewButton)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(versionLbl)
					.addGap(2))
		);
		contentPane.setLayout(gl_contentPane);

		weeklyBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				//				PrintDir(weeklyDir);
				try {
					begin(weeklyBtn, weeklyDir);
				} catch (InterruptedException e1) {
					JOptionPane.showMessageDialog(null,"Error has occurred!","Alert",JOptionPane.WARNING_MESSAGE);
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		f2Btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				//				PrintDir(f2Dir);
				try {
					begin(f2Btn, f2Dir);
				} catch (InterruptedException e1) {
					JOptionPane.showMessageDialog(null,"Error has occurred!","Alert",JOptionPane.WARNING_MESSAGE);
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		f1Btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				//				PrintDir(f1Dir);
				try {
					begin(f1Btn, f1Dir);
				} catch (InterruptedException e1) {
					JOptionPane.showMessageDialog(null,"Error has occurred!","Alert",JOptionPane.WARNING_MESSAGE);
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}
}
