package src;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

public class ChatClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final Font MEIRYO_FONT_14 = new Font("Meiryo", Font.PLAIN, 14);
	private static final Font MEIRYO_FONT_16 = new Font("Meiryo", Font.PLAIN, 16);
	private static final Border BLANK_BORDER = BorderFactory.createEmptyBorder(10, 10, 20, 10);// top,r,b,l

	private JPanel textPanel, inputPanel;
	private JTextField textField;
	private String name, message;
	private ChatClient chatClient;
	private JList<String> userList, channelList;
	private DefaultListModel<String> listModel;

	protected JTextArea textArea, userArea;
	protected JFrame frame;
	protected JButton privateMsgButton, startButton, sendButton;
	protected JPanel clientPanel, userPanel, chatTextPanel;
	protected JTabbedPane channelPanel;

	public ChatClientGUI() {

		frame = new JFrame("Client Chat Console");

		/*
		 * intercept close method, inform server we are leaving
		 */
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				if (chatClient != null) {
					try {
						sendMessage("Bye all, I am leaving");
						chatClient.serverIF.leaveChat(name);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		});

		Container container = getContentPane();
		JPanel outerPanel = new JPanel(new BorderLayout());
		
		chatTextPanel = getTextPanel();

		outerPanel.add(getInputPanel(), BorderLayout.CENTER);
		outerPanel.add(chatTextPanel, BorderLayout.NORTH);

		JPanel leftPanel = new JPanel(new BorderLayout());
		channelPanel = getChannelPanel();
		
		leftPanel.add(channelPanel, BorderLayout.NORTH);
		leftPanel.add(getUsersPanel(), BorderLayout.SOUTH);

		container.setLayout(new BorderLayout());
		container.add(outerPanel, BorderLayout.CENTER);
		container.add(leftPanel, BorderLayout.WEST);

		frame.add(container);
		frame.pack();
		frame.setAlwaysOnTop(true);
		frame.setLocation(150, 150);
		textField.requestFocus();

		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Method to set up the JPanel to display the chat text
	 * 
	 * @return
	 */
	public JPanel getTextPanel() {
		String welcome = "Welcome enter your name and press Start to begin\n";
		textArea = new JTextArea(welcome, 14, 34);
		textArea.setMargin(new Insets(1, 1, 1, 1));
		textArea.setFont(MEIRYO_FONT_14);

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		textPanel = new JPanel(new BorderLayout());
		textPanel.add(scrollPane);

		textPanel.setFont(MEIRYO_FONT_14);
		return textPanel;
	}

	/**
	 * Method to build the panel with input field
	 * 
	 * @return inputPanel
	 */
	public JPanel getInputPanel() {
		inputPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		inputPanel.setBorder(BLANK_BORDER);
		textField = new JTextField();
		textField.setFont(MEIRYO_FONT_14);
		inputPanel.add(textField);
		return inputPanel;
	}

	/**
	 * Method to build the panel displaying currently connected users with a call to
	 * the button panel building method
	 * 
	 * @return
	 */
	public JPanel getUsersPanel() {

		userPanel = new JPanel(new BorderLayout());
		String userStr = " Current Users      ";

		JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
		userPanel.add(userLabel, BorderLayout.NORTH);
		userLabel.setFont(MEIRYO_FONT_16);

		String[] noClientsYet = { "No other users" };
		setClientPanel(noClientsYet);

		clientPanel.setFont(MEIRYO_FONT_14);
		userPanel.add(makeButtonPanel(), BorderLayout.SOUTH);
		userPanel.setBorder(BLANK_BORDER);

		return userPanel;
	}

	/**
	 * Builds channel list panel
	 * 
	 * @return JPanel channel list panel
	 */
	public JTabbedPane getChannelPanel() {

		channelPanel = new JTabbedPane();
		String pannelTitle = "TEXT CHANNELS";

		JLabel channelLabel = new JLabel(pannelTitle, JLabel.CENTER);
		channelPanel.add(channelLabel, BorderLayout.NORTH);
		channelLabel.setFont(MEIRYO_FONT_16);

		String[] channels = { "general", "off-topic", "middleware" };
		setChannelPanel(channels);

		channelPanel.setFont(MEIRYO_FONT_14);
		channelPanel.setBorder(BLANK_BORDER);

		return channelPanel;
	}

	/**
	 * Populate current user panel with a selectable list of currently connected
	 * users
	 * 
	 * @param currClients
	 */
	public void setClientPanel(String[] currClients) {
		clientPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel<String>();

		listModel.addAll(Arrays.asList(currClients));
		if (currClients.length > 1) {
			privateMsgButton.setEnabled(true);
		}

		// Create the list and put it in a scroll pane.
		userList = new JList<String>(listModel);
		userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		userList.setVisibleRowCount(8);
		userList.setFont(MEIRYO_FONT_14);
		JScrollPane listScrollPane = new JScrollPane(userList);

		clientPanel.add(listScrollPane, BorderLayout.CENTER);
		userPanel.add(clientPanel, BorderLayout.CENTER);
	}

	/**
	 * Populate channels panel
	 * 
	 * @param channelTitles
	 */
	public void setChannelPanel(String[] channelTitles) {
		DefaultListModel<String> channelListModel = new DefaultListModel<String>();
		channelListModel.addAll(Arrays.asList(channelTitles));

		// Create the list and put it in a scroll pane.
		channelList = new JList<String>(channelListModel);
		channelList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		channelList.setVisibleRowCount(8);
		channelList.setFont(MEIRYO_FONT_14);
		channelPanel.add(new JScrollPane(channelList), BorderLayout.CENTER);
	}

	/**
	 * Make the buttons and add the listener
	 * 
	 * @return
	 */
	public JPanel makeButtonPanel() {
		sendButton = new JButton("Send ");
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);

		privateMsgButton = new JButton("Send PM");
		privateMsgButton.addActionListener(this);
		privateMsgButton.setEnabled(false);

		startButton = new JButton("Start ");
		startButton.addActionListener(this);

		JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
		buttonPanel.add(privateMsgButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(startButton);
		buttonPanel.add(sendButton);

		return buttonPanel;
	}

	/**
	 * Action handling on the buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		try {
			// get connected to chat service
			if (e.getSource() == startButton) {
				name = textField.getText();
				if (name.length() != 0) {
					frame.setTitle(name + "'s console ");
					textField.setText("");
					textArea.append("username : " + name + " connecting to chat...\n");
					getConnected(name);
					if (!chatClient.connectionProblem) {
						startButton.setEnabled(false);
						sendButton.setEnabled(true);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "Enter your name to Start");
				}
			}

			// get text and clear textField
			if (e.getSource() == sendButton) {
				message = textField.getText();
				textField.setText("");
				sendMessage(message);
				System.out.println("Sending message : " + message);
			}

			// send a private message, to selected users
			if (e.getSource() == privateMsgButton) {
				int[] privateList = userList.getSelectedIndices();

				for (int i = 0; i < privateList.length; i++) {
					System.out.println("selected index :" + privateList[i]);
				}
				message = textField.getText();
				textField.setText("");
				sendPrivate(privateList);
			}

		} catch (RemoteException remoteExc) {
			remoteExc.printStackTrace();
		}

	}

	/**
	 * Send a message, to be relayed to all chatters
	 */
	private void sendMessage(String chatMessage) throws RemoteException {
		chatClient.serverIF.updateChat(name, chatMessage);
	}

	/**
	 * Send a message, to be relayed, only to selected chatters
	 */
	private void sendPrivate(int[] privateList) throws RemoteException {
		String privateMessage = "[PM from " + name + "] :" + message + "\n";
		chatClient.serverIF.sendPM(privateList, privateMessage);
	}

	/**
	 * Make the connection to the chat server
	 */
	private void getConnected(String userName) throws RemoteException {
		String cleanedUserName = userName.replaceAll("\\s+", "_");
		cleanedUserName = userName.replaceAll("\\W+", "_");
		try {
			chatClient = new ChatClient(this, cleanedUserName);
			chatClient.startClient();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
