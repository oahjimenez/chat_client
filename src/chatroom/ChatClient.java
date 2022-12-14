package chatroom;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import chatroom.util.Constants;

/**
 * Client implementation of remote interface
 */
public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {

	private static final long serialVersionUID = 7468891722773409712L;

	private static final Logger log = Logger.getLogger(ChatClient.class.getName());

	private static final String HOSTNAME = "localhost";
	private static final int COM_PORT = 1009;
	private static final String SERVICE_NAME = "GroupChatService";
	private static final String USER_LISTENER_SERVICE_NAME = "ClientListenService";
	private static final String RMI_URI = String.format("rmi://%s:%s/", HOSTNAME, COM_PORT);

	private static final String SERVER_EXCEPTION_TITLE = "Server Exception";

	protected ChatClientGUI chatGUI;
	protected String clientServiceName, userName;
	protected ChatServerInterface serverIF;
	protected boolean connectionProblem = Boolean.FALSE;

	public ChatClient(ChatClientGUI aChatGUI, String userName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.userName = userName;
		this.clientServiceName = USER_LISTENER_SERVICE_NAME.concat("_").concat(userName);
	}

	/**
	 * Register our own listening service/interface lookup the server RMI interface,
	 * then send our details
	 * 
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public void startClient() throws RemoteException, MalformedURLException {
		String[] details = { userName, HOSTNAME, String.valueOf(COM_PORT), clientServiceName };
		try {
			serverIF = (ChatServerInterface) Naming.lookup(RMI_URI + SERVICE_NAME);
		} catch (Exception e) {
			log.severe(e.getMessage());
			JOptionPane.showMessageDialog(chatGUI.frame, Constants.Messages.SERVER_UNAVAILABLE_MESSAGE,
					Constants.Messages.CONNECTION_PROBLEM_MESSAGE, JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
		}
		if (!connectionProblem) {
			userLogin(details);
		}
		log.info("Client Listen RMI Server is running...\n");
	}

	/**
	 * Checks if username is available and request a server suscription
	 * 
	 * @param details
	 */
	private void userLogin(String[] details) {
		try {
			if (serverIF.isUsernameAvailable(userName)) {
				Naming.rebind(RMI_URI + clientServiceName, this);
				registerWithServer(details);
			} else {
				connectionProblem = true;
				chatGUI.showUsernameNotAvailableDialog(userName);
			}
		} catch (Exception e) {
			connectionProblem = true;
			log.severe(e.getMessage());
		}
	}

	/**
	 * pass our username, hostname and RMI service name to the server to register
	 * out interest in joining the chat
	 * 
	 * @param details
	 * @throws RemoteException
	 */
	public void registerWithServer(String[] details) throws RemoteException {
		serverIF.passIDentity(this.ref);
		serverIF.registerListener(details);
	}

	/**
	 * Receive a string from the chat server
	 */
	@Override
	public void messageFromServerToChannel(String message, String channel) throws RemoteException {
		log.info(message);
		chatGUI.messageFromServerToChannel(message, channel);
	}

	/**
	 * Receive a exception from the chat server
	 */
	@Override
	public void exceptionFromServer(String message) throws RemoteException {
		chatGUI.displayModal(SERVER_EXCEPTION_TITLE, message);
	}

	/**
	 * A method to update the display of users currently connected to the server
	 */
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {
		chatGUI.updateClientPanel(currentUsers);
	}

	@Override
	public void serverIsClosing() throws RemoteException {
		chatGUI.serverIsClosing();
	}
}
