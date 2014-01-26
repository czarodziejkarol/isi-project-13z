package com.carlncarl.isi.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
	private boolean debug = false;
	private Executor executor;
	
	public UI() {
		executor = new Executor(this);
		previousInputs = new LinkedList<String>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 639, 480);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);

		chckbxmntmDebug = new JCheckBoxMenuItem("Debug");
		chckbxmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDebugMode();
			}
		});

		mntmShowKnowledge = new JMenuItem("Show knowledge");
		mntmShowKnowledge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showKnowledge();
			}
		});
		
		JMenuItem mntmWczytajWiedz = new JMenuItem("Wczytaj wiedz\u0119");
		mnOptions.add(mntmWczytajWiedz);
		mnOptions.add(mntmShowKnowledge);

		mnOptions.add(chckbxmntmDebug);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblInput = new JLabel("Input:");
		lblInput.setBounds(10, 11, 77, 14);
		contentPane.add(lblInput);

		textFieldInput = new JTextField();
		textFieldInput.setBounds(10, 36, 518, 20);
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
		contentPane.add(textFieldInput);
		textFieldInput.setColumns(10);

		lblOutput = new JLabel("Output:");
		lblOutput.setBounds(10, 67, 46, 14);
		contentPane.add(lblOutput);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 93, 603, 316);
		contentPane.add(scrollPane);

		txtOutput = new JTextArea();
		scrollPane.setViewportView(txtOutput);
		DefaultCaret caret = (DefaultCaret) txtOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JButton btnSend = new JButton("Send");
		btnSend.setBounds(538, 35, 75, 23);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				receiveQuery();
			}
		});
		contentPane.add(btnSend);
		setTitle("Renaissance Painters");
	}

	protected void showKnowledge() {

		String knowledge = "";
		for (Fact fact : executor.getFacts() ) {
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
		this.debug  = chckbxmntmDebug.isSelected();
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
