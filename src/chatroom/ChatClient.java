package chatroom;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {

	private static final long serialVersionUID = 7468891722773409712L;

	private static final String HOSTNAME = "localhost";
	private static final int COM_PORT = 1009;
	private static final String SERVICE_NAME = "GroupChatService";
	private static final String RMI_URI = String.format("rmi://%s:%s/", HOSTNAME, COM_PORT);
	private static final String SERVER_UNAVAILABLE_MESSAGE = "The server seems to be unavailable\nPlease try later";
	private static final String CONNECTION_PROBLEM_MESSAGE = "Connection problem";

	protected ChatClientGUI chatGUI;
	private String clientServiceName, userName;
	protected ChatServerInterface serverIF;
	protected boolean connectionProblem = Boolean.FALSE;

	private static final Logger log = Logger.getLogger(ChatClient.class.getName());

	public ChatClient(ChatClientGUI aChatGUI, String userName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.userName = userName;
		this.clientServiceName = "ClientListenService_" + userName;
	}

	public ChatServerInterface getRemoteServer() throws MalformedURLException, RemoteException, NotBoundException {
		return (ChatServerInterface) Naming.lookup(RMI_URI + SERVICE_NAME);
	}

	/**
	 * Register our own listening service/interface lookup the server RMI interface,
	 * then send our details
	 * 
	 * @throws RemoteException
	 */
	public void startClient() throws RemoteException {
		String[] details = { userName, HOSTNAME, String.valueOf(COM_PORT), clientServiceName };
		try {
			Naming.rebind(RMI_URI + clientServiceName, this);
			serverIF = getRemoteServer();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(chatGUI.frame, SERVER_UNAVAILABLE_MESSAGE, CONNECTION_PROBLEM_MESSAGE,
					JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			log.severe(e.getMessage());
		}
		if (!connectionProblem) {
			registerWithServer(details);
		}
		log.info("Client Listen RMI Server is running...\n");
	}

	/**
	 * pass our username, hostname and RMI service name to the server to register
	 * out interest in joining the chat
	 * 
	 * @param details
	 */
	public void registerWithServer(String[] details) {
		try {
			serverIF.passIDentity(this.ref);
			serverIF.registerListener(details);
		} catch (RemoteException e) {
			log.severe(e.getMessage());
		} catch (Exception e) {
			connectionProblem=true;
			JOptionPane.showMessageDialog(chatGUI.frame, e.getMessage(), CONNECTION_PROBLEM_MESSAGE,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Receive a string from the chat server
	 */
	@Override
	public void messageFromServerToChannel(String message, String channel) throws RemoteException {
		log.info(message);
		if (channel.equals("#pm")) {
			chatGUI.getCurrentTextArea().append(message);
			chatGUI.conversationTextArea.append(message);
			chatGUI.conversationTextArea.setCaretPosition(chatGUI.conversationTextArea.getDocument().getLength());
		} else {
			chatGUI.appendTextToChatTextAreaForChannel(message,channel);
			if (chatGUI.selectedChannel.getTitle().equals(channel)) {
				chatGUI.conversationTextArea.append(message);
				chatGUI.conversationTextArea.setCaretPosition(chatGUI.conversationTextArea.getDocument().getLength());
			}
		}
	}
	
	/**
	 * Receive a exception from the chat server
	 */
	@Override
	public void exceptionFromServer(String message) throws RemoteException {
		log.warning(message);
		chatGUI.conversationTextArea.append("Exception:["+message+"]\n");
		chatGUI.conversationTextArea.setCaretPosition(chatGUI.conversationTextArea.getDocument().getLength());
	}
	
	/**
	 * A method to update the display of users currently connected to the server
	 */
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {
		if (currentUsers.length < 2) {
			chatGUI.privateMsgButton.setEnabled(false);
		}
		chatGUI.updateClientPanel(currentUsers);
	}
}
