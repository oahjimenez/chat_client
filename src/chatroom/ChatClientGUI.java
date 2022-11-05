package chatroom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import chatroom.domain.Channel;

public class ChatClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final Font MEIRYO_FONT_14 = new Font("Meiryo", Font.PLAIN, 14);
	private static final Font MEIRYO_FONT_16_BOLD = new Font("Meiryo", Font.BOLD, 16);
	private static final Border RIGHT_BLANK_BORDER = BorderFactory.createEmptyBorder(20, 10, 20, 20);// top,r,b,l
	private static final Border LEFT_BLANK_BORDER = BorderFactory.createEmptyBorder(20, 20, 20, 10);// top,r,b,l
	private static final int CHAT_WIDTH = 60;

	private static final String APP_TITLE = "Discord Lite ™";

	private static final String WELCOME_MESSAGE = "Welcome enter your name and press Start to begin\n";
	private static final String LOGOUT_MESSAGE = "Bye all, I am leaving";
	private static final String CHANNEL_BEFORE_LOGIN_MESSAGE = "Login to get started";
	private static final String NEW_LINE = System.lineSeparator();
	private static final String SINGLE_SPACE = " ";

	private JTextField textField;
	private String name, message;
	private ChatClient chatClient;
	private JList<String> userList, channelList;
	private DefaultListModel<String> listModel, channelListModel;

	protected JTextArea userArea;
	protected JFrame frame;
	protected JButton privateMsgButton, startButton, sendButton;
	protected JPanel clientPanel, userPanel, textPanel, inputPanel, channelPanel;
	protected JLabel channelLabel;

	protected Map<Channel, JTextArea> channelChatContents = new LinkedHashMap<>();
	protected Channel selectedChannel;
	protected JTextArea conversationTextArea;

	protected Container container;

	protected static final Logger log = Logger.getLogger(ChatClientGUI.class.getName());

	public JTextArea getCurrentTextArea() {
		return channelChatContents.get(selectedChannel);
	}

	protected JTextArea createTextArea(String text) {
		JTextArea textArea = new JTextArea(text, 14, CHAT_WIDTH);
		textArea.setMargin(new Insets(1, 1, 1, 1));
		textArea.setFont(MEIRYO_FONT_14);

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		return textArea;
	}

	protected void initChannelContents(Collection<String> channelTitles) {
		this.channelChatContents.clear();
		for (String channelTitle : channelTitles) {
			String textAreaMessage = String.join(SINGLE_SPACE, WELCOME_MESSAGE, channelTitle, NEW_LINE);
			this.channelChatContents.put(Channel.fromTitle(channelTitle), createTextArea(textAreaMessage));
		}
	}

	public ChatClientGUI() {
		String[] channelTitles = { CHANNEL_BEFORE_LOGIN_MESSAGE };
		initChannelContents(Arrays.asList(channelTitles));
		selectedChannel = Channel.fromTitle(channelTitles[0]); // general channel by default

		frame = new JFrame(APP_TITLE);

		/*
		 * intercept close method, inform server we are leaving
		 */
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				if (chatClient != null && chatClient.serverIF != null) {
					try {
						sendMessage(LOGOUT_MESSAGE);
						chatClient.serverIF.leaveChat(name, selectedChannel.getTitle());
					} catch (RemoteException e) {
						log.severe(e.getMessage());
					}
				}
				System.exit(0);
			}
		});

		container = getContentPane();
		JPanel containerPanel = new GradientPanel(new BorderLayout());
		JPanel outerPanel = new JPanel(new BorderLayout());

		conversationTextArea = createTextArea(
				String.join(SINGLE_SPACE, WELCOME_MESSAGE, selectedChannel.getTitle(), NEW_LINE));
		textPanel = new JPanel(new BorderLayout());
		textPanel.add(new JScrollPane(conversationTextArea));
		textPanel.setFont(MEIRYO_FONT_14);
		textPanel.setBorder(RIGHT_BLANK_BORDER);

		JPanel inputPanel = getInputPanel();
		outerPanel.add(inputPanel, BorderLayout.CENTER);
		outerPanel.add(textPanel, BorderLayout.NORTH);

		JPanel leftPanel = new JPanel(new BorderLayout());
		channelPanel = getChannelPanel(channelTitles);
		userPanel = getUsersPanel();

		leftPanel.add(channelPanel, BorderLayout.NORTH);
		leftPanel.add(userPanel, BorderLayout.SOUTH);

		container.setLayout(new BorderLayout());

		inputPanel.setOpaque(false);
		textPanel.setOpaque(false);
		outerPanel.setOpaque(false);
		leftPanel.setOpaque(false);
		channelPanel.setOpaque(false);
		userPanel.setOpaque(false);

		containerPanel.add(outerPanel, BorderLayout.CENTER);
		containerPanel.add(leftPanel, BorderLayout.WEST);

		container.add(containerPanel, BorderLayout.CENTER);

		frame.add(container);
		frame.pack();
		frame.setAlwaysOnTop(true);
		frame.setLocation(150, 150);
		textField.requestFocus();

		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Method to build the panel with input field
	 * 
	 * @return inputPanel
	 */
	public JPanel getInputPanel() {
		inputPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		inputPanel.setBorder(RIGHT_BLANK_BORDER);
		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && sendButton.isEnabled()) {
					try {
						message = textField.getText();
						textField.setText("");
						sendMessage(message);
						System.out.println("Sending message : " + message);

					} catch (RemoteException error) {
						log.severe(error.getMessage());
					}
				}
			}
		});

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
		String userStr = "Current Users";

		JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
		userPanel.add(userLabel, BorderLayout.NORTH);
		userLabel.setFont(MEIRYO_FONT_16_BOLD);
		userLabel.setForeground(Color.WHITE);

		String[] noClientsYet = { "No other users" };
		setClientPanel(noClientsYet);

		clientPanel.setFont(MEIRYO_FONT_14);
		userPanel.add(makeButtonPanel(), BorderLayout.SOUTH);
		userPanel.setBorder(LEFT_BLANK_BORDER);

		return userPanel;
	}

	/**
	 * Builds channel list panel
	 * 
	 * @return JPanel channel list panel
	 */
	public JPanel getChannelPanel(String... channels) {

		channelPanel = new JPanel(new BorderLayout());
		String pannelTitle = "Channels";

		channelLabel = new JLabel(pannelTitle, JLabel.CENTER);
		channelPanel.add(channelLabel, BorderLayout.NORTH);
		channelLabel.setFont(MEIRYO_FONT_16_BOLD);
		channelLabel.setForeground(Color.WHITE);

		channelListModel = new DefaultListModel<String>();
		channelListModel.addAll(Arrays.asList(channels));

		// Create the list and put it in a scroll pane.
		channelList = new JList<String>(channelListModel);
		channelList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		channelList.setVisibleRowCount(8);
		channelList.setFont(MEIRYO_FONT_14);
		channelList.setSelectedIndex(0); // first channel general by default
		channelPanel.add(new JScrollPane(channelList), BorderLayout.CENTER);

		channelPanel.setFont(MEIRYO_FONT_14);
		channelPanel.setBorder(LEFT_BLANK_BORDER);

		return channelPanel;
	}

	protected JPanel makeTextPanel(String text) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		// panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		return panel;
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
					textField.setText("");
					getCurrentTextArea().append("username : " + name + " connecting to chat...\n");
					getConnected(name);
					if (!chatClient.connectionProblem) {
						startButton.setEnabled(false);
						sendButton.setEnabled(true);

						try {
							loadChannelAfterLogin();
						} catch (RemoteException e1) {
							e1.printStackTrace();
						}
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
			log.severe(remoteExc.getMessage());
		}

	}

	/**
	 * Send a message, to be relayed to all chatters
	 */
	private void sendMessage(String chatMessage) throws RemoteException {
		chatClient.serverIF.updateChat(name, chatMessage, selectedChannel.getTitle());
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
			log.severe(e.getMessage());
		}
	}

	public void updateClientPanel(String[] currentUsers) {
		listModel.clear();
		listModel.addAll(Arrays.asList(currentUsers));
		userPanel.revalidate();
	}

	private void loadChannelAfterLogin() throws RemoteException {
		List<String> channelTitles = chatClient.serverIF.getChannelsName();
		initChannelContents(channelTitles);
		selectedChannel = Channel.fromTitle(channelTitles.stream().findFirst().get()); // general channel by default
		channelListModel.clear();
		channelListModel.addAll(channelTitles);

		channelList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					String oldChannel = selectedChannel.getTitle();
					selectedChannel = Channel.fromTitle(channelList.getSelectedValue());
					conversationTextArea.setText(channelChatContents.get(selectedChannel).getText());
					conversationTextArea.setCaretPosition(conversationTextArea.getDocument().getLength());

					try {
						chatClient.serverIF.goToChannel(name, selectedChannel.getTitle(), oldChannel);
						System.out.println("After Login Selected channel + " + selectedChannel);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});

		channelList.setSelectedIndex(0); // select first channel by default
		channelPanel.revalidate();
		channelPanel.repaint();
	}
}
