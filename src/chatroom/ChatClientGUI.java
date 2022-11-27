package chatroom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import chatroom.domain.Channel;
import chatroom.util.Constants;

/**
 * Client graphical interface
 */
public class ChatClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = -989721431765931172L;

	protected static final Logger log = Logger.getLogger(ChatClientGUI.class.getName());

	private static final Font MEIRYO_FONT_14 = new Font("Meiryo", Font.PLAIN, 14);
	private static final Font MEIRYO_FONT_16 = new Font("Meiryo", Font.PLAIN, 16);
	private static final Border RIGHT_BLANK_BORDER = BorderFactory.createEmptyBorder(20, 10, 20, 20);// top,r,b,l
	private static final Border LEFT_BLANK_BORDER = BorderFactory.createEmptyBorder(20, 20, 20, 10);// top,r,b,l

	private static final int CHAT_WIDTH = 60;
	private static final int CHANNEL_PREFFERED_HEIGHT = 220;
	private static final int SPECIAL_CHANNEL_PREFFERED_HEIGHT = 200;

	private static final int MAIN_FRAME_MINIMUM_WIDHT = 600;
	private static final int MAIN_FRAME_MINIMUM_HEIGHT = 800;

	private static final String APP_TITLE = "Discord Lite ™";

	private static final String SPECIAL_CHANNEL_INFINI = "#infini";
	private static final String SPECIAL_CHANNEL_SPEAKUP = "#speak-up";

	private static final Map<String, String> SPECIAL_CHANNEL_MESSAGES = Map.of(SPECIAL_CHANNEL_INFINI,
			Constants.Messages.INFINI_MESSAGE, SPECIAL_CHANNEL_SPEAKUP, Constants.Messages.SPEAK_UP_MESSAGE);

	private String username;
	private Channel selectedChannel;

	private String message;
	private ChatClient chatClient;

	private JList<String> userList, channelList, specialChannelList;

	protected JFrame frame;
	protected Container container;

	protected JTextArea userArea, textField, conversationTextArea;
	protected JButton privateMsgButton, startButton, sendButton, speakUpButton;
	private DefaultListModel<String> listModel, channelListModel, specialChannelListModel;
	private JPanel clientPanel, userPanel, textPanel, inputPanel, channelPanel, specialChannelPanel;

	protected Map<Channel, JTextArea> channelChatContents = new LinkedHashMap<>();

	protected boolean hasInfiniChannelBeenAccessedOnce;

	public ChatClientGUI() {
		initGUIContents();
	}

	public void initGUIContents() {
		String[] channelTitles = { Constants.Messages.CHANNEL_BEFORE_LOGIN_MESSAGE };
		initChannelContents(Arrays.asList(channelTitles));
		selectedChannel = Channel.fromTitle(channelTitles[0]); // general channel by default

		frame = new JFrame(APP_TITLE);
		frame.setMinimumSize(new Dimension(MAIN_FRAME_MINIMUM_WIDHT, MAIN_FRAME_MINIMUM_HEIGHT));

		/*
		 * intercept close method, inform server we are leaving
		 */
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				if (chatClient != null && chatClient.serverIF != null) {
					try {
						sendMessage(Constants.Messages.LOGOUT_MESSAGE);
						chatClient.serverIF.leaveChat(username);
					} catch (RemoteException e) {
						log.severe(e.getMessage());
					}
				}
				System.exit(0);
			}
		});

		container = getContentPane();
		JPanel outerPanel = new JPanel(new BorderLayout());

		conversationTextArea = createTextArea(String.join(Constants.SINGLE_SPACE, Constants.Messages.WELCOME_MESSAGE,
				selectedChannel.getTitle(), Constants.NEW_LINE));
		textPanel = new JPanel(new BorderLayout());
		textPanel.add(new JScrollPane(conversationTextArea));
		textPanel.setFont(MEIRYO_FONT_14);
		textPanel.setBorder(RIGHT_BLANK_BORDER);

		outerPanel.add(getInputPanel(), BorderLayout.CENTER);
		outerPanel.add(textPanel, BorderLayout.NORTH);

		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel channelContainerPanel = new JPanel(new BorderLayout());

		channelPanel = getChannelPanel(channelTitles);
		channelPanel.setPreferredSize(new Dimension(channelPanel.getPreferredSize().width, CHANNEL_PREFFERED_HEIGHT));
		specialChannelPanel = getSpecialChannelPanel(channelTitles);
		specialChannelPanel.setPreferredSize(
				new Dimension(specialChannelPanel.getPreferredSize().width, SPECIAL_CHANNEL_PREFFERED_HEIGHT));

		channelContainerPanel.add(channelPanel, BorderLayout.NORTH);
		channelContainerPanel.add(specialChannelPanel, BorderLayout.SOUTH);

		userPanel = getUsersPanel();

		leftPanel.add(channelContainerPanel, BorderLayout.NORTH);
		leftPanel.add(userPanel, BorderLayout.SOUTH);

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

	public JTextArea getCurrentTextArea() {
		return channelChatContents.get(selectedChannel);
	}

	public void appendTextToChatTextAreaForChannel(String msg, String channelName) {
		for (Map.Entry<Channel, JTextArea> set : channelChatContents.entrySet()) {
			if (set.getKey().getTitle().equals(channelName)) {
				set.getValue().append(msg);
			}
		}
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
			String channelMessage = SPECIAL_CHANNEL_MESSAGES.containsKey(channelTitle)
					? SPECIAL_CHANNEL_MESSAGES.get(channelTitle)
					: Constants.Messages.WELCOME_MESSAGE;
			String textAreaMessage = String.join(Constants.SINGLE_SPACE, channelMessage, channelTitle,
					Constants.NEW_LINE);
			this.channelChatContents.put(Channel.fromTitle(channelTitle), createTextArea(textAreaMessage));
		}
	}

	/**
	 * Method to build the panel with input field
	 *
	 * @return inputPanel
	 */
	public JPanel getInputPanel() {
		inputPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		inputPanel.setBorder(RIGHT_BLANK_BORDER);
		textField = new JTextArea();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				String input = textField.getText().trim();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !input.isEmpty()) {
					if (sendButton.isEnabled()) {
						try {
							sendMessage(input);
						} catch (RemoteException error) {
							log.severe(error.getMessage());
						}
					} else {
						try {
							registerUser();
						} catch (RemoteException ex) {
							throw new RuntimeException(ex);
						}
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
		String userStr = " Current Users      ";

		JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
		userPanel.add(userLabel, BorderLayout.NORTH);
		userLabel.setFont(MEIRYO_FONT_16);

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

		JLabel channelLabel = new JLabel(pannelTitle, JLabel.CENTER);
		channelPanel.add(channelLabel, BorderLayout.NORTH);
		channelLabel.setFont(MEIRYO_FONT_16);

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

	/**
	 * Builds channel list panel
	 *
	 * @return JPanel channel list panel
	 */
	public JPanel getSpecialChannelPanel(String... channels) {

		specialChannelPanel = new JPanel(new BorderLayout());
		String pannelTitle = "Special Channels";

		JLabel specialChannelLabel = new JLabel(pannelTitle, JLabel.CENTER);
		specialChannelPanel.add(specialChannelLabel, BorderLayout.NORTH);
		specialChannelLabel.setFont(MEIRYO_FONT_16);

		specialChannelListModel = new DefaultListModel<String>();
		specialChannelListModel.addAll(Arrays.asList(channels));

		// Create the list and put it in a scroll pane.
		specialChannelList = new JList<String>(specialChannelListModel);
		specialChannelList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		specialChannelList.setVisibleRowCount(8);
		specialChannelList.setFont(MEIRYO_FONT_14);
		specialChannelList.setSelectedIndex(0); // first channel general by default
		specialChannelPanel.add(new JScrollPane(specialChannelList), BorderLayout.CENTER);

		specialChannelPanel.setFont(MEIRYO_FONT_14);
		specialChannelPanel.setBorder(LEFT_BLANK_BORDER);

		return specialChannelPanel;
	}

	protected JComponent makeTextPanel(String text) {
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
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
		// Highlight current user
		userList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 5773379957985135856L;

			@Override
			@SuppressWarnings("rawtypes")
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				String cellUsername = (String) value;
				if (cellUsername.equals(username)) {
					setBackground(Color.LIGHT_GRAY);
				}
				return this;
			}
		});

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

		speakUpButton = new JButton("Speak Up");
		speakUpButton.setEnabled(false);
		speakUpButton.addActionListener(this);

		JPanel buttonPanel = new JPanel(new GridLayout(5, 1));
		buttonPanel.add(privateMsgButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(speakUpButton);
		buttonPanel.add(startButton);
		buttonPanel.add(sendButton);

		return buttonPanel;
	}

	/**
	 * Send a message, to be relayed to all chatters
	 */
	private void sendMessage(String chatMessage) throws RemoteException {
		chatClient.serverIF.updateChat(username, chatMessage, selectedChannel.getTitle());
		textField.setText("");
		log.info("Sending message : " + chatMessage);
	}

	private void registerUser() throws RemoteException {
		username = textField.getText().trim();
		if (!username.isEmpty()) {
			frame.setTitle(username + "'s console ");
			textField.setText("");
			getCurrentTextArea().append("username : " + username + " connecting to chat...\n");
			getConnected(username);
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

	/**
	 * Send a message, to be relayed, only to selected chatters
	 */
	private void sendPrivate(int[] privateList) throws RemoteException {
		String privateMessage = "[PM from " + username + "] :" + message + "\n";
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
		if (currentUsers.length < 2) {
			privateMsgButton.setEnabled(false);
		}
		listModel.clear();
		listModel.addAll(Arrays.asList(currentUsers));
		userPanel.revalidate();
	}

	public void displayModal(String title, String message) {
		log.warning(message);

		JLabel modalLabel = new JLabel(message, JLabel.CENTER);
		modalLabel.setFont(MEIRYO_FONT_16);
		JDialog dialog = new JDialog(frame, title);
		dialog.add(modalLabel);
		dialog.setModal(false); // IMPORTANT! Now the thread isn't blocked
		dialog.setSize(500, 200);
		dialog.setLocationRelativeTo(null); // center of screen
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	private void loadChannelAfterLogin() throws RemoteException {
		List<String> channelTitles = chatClient.serverIF.getChannelsName();
		List<String> regularChannelTitles = channelTitles.stream()
				.filter(channel -> !SPECIAL_CHANNEL_MESSAGES.containsKey(channel)).collect(Collectors.toList());
		List<String> specialChannelTitles = channelTitles.stream()
				.filter(channel -> SPECIAL_CHANNEL_MESSAGES.containsKey(channel)).collect(Collectors.toList());

		initChannelContents(channelTitles);
		selectedChannel = Channel.fromTitle(channelTitles.stream().findFirst().get()); // general channel by default
		channelListModel.clear();
		channelListModel.addAll(regularChannelTitles);

		specialChannelListModel.clear();
		specialChannelListModel.addAll(specialChannelTitles);

		channelList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || channelList.getSelectedIndex() < 0) {
					return;
				}
				updateRegularChannelSelection();
			}
		});

		specialChannelList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || specialChannelList.getSelectedIndex() < 0) {
					return;
				}
				updateSpecialChannelSelection();
			}

		});

		channelList.setSelectedIndex(0); // select first channel by default
		channelPanel.revalidate();
		channelPanel.repaint();
	}

	protected void updateRegularChannelSelection() {
		specialChannelList.clearSelection();
		String oldChannelName = Optional.ofNullable(selectedChannel.getTitle()).orElse(Constants.EMPTY_STRING);

		selectedChannel = Channel.fromTitle(channelList.getSelectedValue());
		conversationTextArea.setText(channelChatContents.get(selectedChannel).getText());
		conversationTextArea.setCaretPosition(conversationTextArea.getDocument().getLength());

		if (sendButton.isEnabled()) {
			if (selectedChannel.getTitle().equals(SPECIAL_CHANNEL_SPEAKUP)) {
				speakUpButton.setEnabled(true);
				textField.setEnabled(false);
			} else {
				if (speakUpButton.isEnabled())
					speakUpButton.setEnabled(false);
				if (!speakUpButton.getText().equals("Speak Up"))
					speakUpButton.setText("Speak Up");
			}

			try {
				if (oldChannelName.equals(SPECIAL_CHANNEL_SPEAKUP)) {
					textField.setEnabled(true);
					if (username.equals(chatClient.serverIF.getSpeakerUsername())) {
						chatClient.serverIF.stopSpeakUp();
					}
				}

				chatClient.serverIF.goToChannel(username, selectedChannel.getTitle());
				System.out.println("After Login Selected channel + " + selectedChannel);
			} catch (RemoteException e) {
				log.severe(e.getMessage());
			}
		}
	}

	protected void updateSpecialChannelSelection() {
		channelList.clearSelection();
		String oldChannelName = Optional.ofNullable(selectedChannel.getTitle()).orElse(Constants.EMPTY_STRING);

		selectedChannel = Channel.fromTitle(specialChannelList.getSelectedValue());
		conversationTextArea.setText(channelChatContents.get(selectedChannel).getText());
		conversationTextArea.setCaretPosition(conversationTextArea.getDocument().getLength());

		if (sendButton.isEnabled()) {
			if (selectedChannel.getTitle().equals(SPECIAL_CHANNEL_SPEAKUP)) {
				speakUpButton.setEnabled(true);
				textField.setEnabled(false);
			} else {
				if (speakUpButton.isEnabled())
					speakUpButton.setEnabled(false);
				if (!speakUpButton.getText().equals("Speak Up"))
					speakUpButton.setText("Speak Up");
			}

			try {
				if (oldChannelName.equals(SPECIAL_CHANNEL_SPEAKUP)) {
					textField.setEnabled(true);
					if (username.equals(chatClient.serverIF.getSpeakerUsername())) {
						chatClient.serverIF.stopSpeakUp();
					}
				}

				chatClient.serverIF.goToChannel(username, selectedChannel.getTitle());
				if (selectedChannel.getTitle().equals(SPECIAL_CHANNEL_INFINI)) {
					if (!hasInfiniChannelBeenAccessedOnce) {
						int val = chatClient.serverIF.getLastInfiniValue();
						String startingMessage = val == 0 ? "Start with number 1"
								: "Last number: " + String.valueOf(val);
						String msg = "[Server] : " + startingMessage + "\n";
						getCurrentTextArea().append(msg);
						conversationTextArea.append(msg);
						conversationTextArea.setCaretPosition(conversationTextArea.getDocument().getLength());
						hasInfiniChannelBeenAccessedOnce = true;
					}
				}
				System.out.println("After Login Selected channel + " + selectedChannel);
			} catch (RemoteException e) {
				log.severe(e.getMessage());
			}
		}
	}

	/**
	 * Action handling on the buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		try {
			// get connected to chat service
			if (e.getSource() == startButton) {
				registerUser();
			}

			// get text and clear textField
			message = textField.getText().trim();
			if (e.getSource() == sendButton && !message.isEmpty()) {
				textField.setText(Constants.EMPTY_STRING);
				sendMessage(message);
				log.info("Sending message : " + message);
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

			if (e.getSource() == speakUpButton) {
				String speakerUsername = chatClient.serverIF.getSpeakerUsername();
				System.out.println("username:" + username);
				if (speakUpButton.getText().equals("Speak Up")) {
					if (speakerUsername == null) {
						chatClient.serverIF.speakUp(this.username);
						textField.setEnabled(true);
						speakUpButton.setText("Hand Over Speech");
					} else {
						JOptionPane.showMessageDialog(frame, "Il y a déja un utilisateur qui a pris la parole!",
								"Speak Up Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					chatClient.serverIF.stopSpeakUp();
					textField.setEnabled(false);
					speakUpButton.setText("Speak Up");
				}
			}

		} catch (RemoteException remoteExc) {
			log.severe(remoteExc.getMessage());
		}

	}

	public void messageFromServerToChannel(String message, String channel) {
		if (channel.equals("#pm")) {
			getCurrentTextArea().append(message);
			conversationTextArea.append(message);
			conversationTextArea.setCaretPosition(conversationTextArea.getDocument().getLength());
			return;
		}
		appendTextToChatTextAreaForChannel(message, channel);
		if (selectedChannel.getTitle().equals(channel)) {
			conversationTextArea.append(message);
			conversationTextArea.setCaretPosition(conversationTextArea.getDocument().getLength());
		}
	}

	public void serverIsClosing() {
		startButton.setEnabled(false);
		sendButton.setEnabled(false);
		privateMsgButton.setEnabled(false);
		speakUpButton.setEnabled(false);
		conversationTextArea.setEnabled(false);
		textField.setEditable(false);
		Timer timer = new Timer(2000, new ActionListener() {
			  @Override
			  public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
				setVisible(false);
				dispose();
			  }
			});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}
}
