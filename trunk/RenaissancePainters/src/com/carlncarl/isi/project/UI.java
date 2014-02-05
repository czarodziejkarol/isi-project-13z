package com.carlncarl.isi.project;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class UI extends JFrame {

	private static final long serialVersionUID = -65267863805109921L;
	private JPanel contentPane;
	private JTextField textFieldInput;
	private JLabel lblOutput;
	private JTextArea txtOutput;
	private LinkedList<String> previousInputs;
	private int current = 0;
	private JScrollPane scrollPane;
	private JCheckBoxMenuItem chckbxmntmDebug;
	private JMenuItem mntmShowKnowledge;
	private JMenuItem mntmWczytajWiedz;
	private boolean debug = false;
	private Executor executor;
	private JPanel panel;
	private JPanel panel_1;

	public UI() {
		executor = new Executor(this);
		previousInputs = new LinkedList<String>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 639, 480);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnOptions = new JMenu("Opcje");
		menuBar.add(mnOptions);

		chckbxmntmDebug = new JCheckBoxMenuItem("Debug");
		chckbxmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDebugMode();
			}
		});

		mntmShowKnowledge = new JMenuItem("Poka\u017C wiedz\u0119");
		mntmShowKnowledge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showKnowledge();
			}
		});

		mntmWczytajWiedz = new JMenuItem("Wczytaj wiedz\u0119");
		mntmWczytajWiedz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readFile();
			}
		});
		mnOptions.add(mntmWczytajWiedz);
		mnOptions.add(mntmShowKnowledge);

		mnOptions.add(chckbxmntmDebug);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(10, 10));
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
				panel.setLayout(new BorderLayout(10, 10));
		
				JLabel lblInput = new JLabel("Zapytanie:");
				panel.add(lblInput, BorderLayout.WEST);
				
						textFieldInput = new JTextField();
						panel.add(textFieldInput);
						textFieldInput.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent e) {
								if (e.getKeyCode() == KeyEvent.VK_ENTER) {
									receiveQuery();
								} else if (e.getKeyCode() == KeyEvent.VK_UP) {
									setPrevious(1);
								} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
									setPrevious(-1);
								}
							}
						});
		textFieldInput.setColumns(10);
		
				JButton btnSend = new JButton("Pytaj!");
				panel.add(btnSend, BorderLayout.EAST);
				btnSend.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						receiveQuery();
					}
				});
		
		panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(10, 10));
		
				lblOutput = new JLabel("Dziennik zdarze\u0144:");
				panel_1.add(lblOutput, BorderLayout.NORTH);

		scrollPane = new JScrollPane();
		panel_1.add(scrollPane);

		txtOutput = new JTextArea();
		txtOutput.setLineWrap(true);
		scrollPane.setViewportView(txtOutput);
		DefaultCaret caret = (DefaultCaret) txtOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		setTitle("O filmach");
	}

	protected void readFile() {
		final JFileChooser fc = new JFileChooser();
		// In response to a button click:
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;

				while ((line = br.readLine()) != null) {
					String[] row = line.split(",");
					if (row.length > 2) {
						executor.addFact(row);
					}
				}
				br.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void showKnowledge() {

		String knowledge = "";
		for (Fact fact : executor.getFacts()) {
			knowledge += "\r\n\t" + fact.toString();
		}

		txtOutput.setText(((txtOutput.getText().length() > 0) ? txtOutput
				.getText() + "\r\n\r\n> " : "")
				+ "Posiadana wiedza: " + "\r\n" + knowledge);

	}

	protected void setPrevious(int i) {
		if (previousInputs.size() > 0) {
			if (i > 0 && current < previousInputs.size() - 1) {
				textFieldInput.setText(previousInputs.get(current + 1));
				current++;
			} else if (i < 0 && current > 0) {
				textFieldInput.setText(previousInputs.get(current - 1));
				current--;
			} else if (current == 0) {
				textFieldInput.setText("");
				current = -1;
			}
		}
	}

	public void showDebugInfo(String debugMessage) {
		txtOutput.setText(((txtOutput.getText().length() > 0) ? txtOutput
				.getText() + "\r\n " : "")
				+ "Debug\t: " + debugMessage);
	}

	// Methods of role USERINTERFACE
	protected void setDebugMode() {
		this.debug = chckbxmntmDebug.isSelected();
	}

	protected void receiveQuery() {
		String inputText = textFieldInput.getText();
		textFieldInput.setText("");
		if (previousInputs.size() == 0
				|| !previousInputs.getFirst().equals(inputText)) {
			previousInputs.addFirst(inputText);
		}
		current = -1;

		String result = receiveQuery(inputText);

		txtOutput.setText(((txtOutput.getText().length() > 0) ? txtOutput
				.getText() + "\r\n\r\n> " : ">")
				+ inputText + "\r\n" + result);

	}

	private String receiveQuery(String inputText) {
		return executor.receiveQuery(inputText);
	}

	public void presentsResults(String inputText, String answer) {
		txtOutput.setText(((txtOutput.getText().length() > 0) ? txtOutput
				.getText() + "\r\n\r\n> " : "")
				+ "Znaleziono odpowiedü na pytanie: \""
				+ inputText
				+ "\"\r\n"
				+ answer);
	}
}