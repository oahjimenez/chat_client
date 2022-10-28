package src;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {

	private static final long serialVersionUID = 7468891722773409712L;
	private static final String HOSTNAME = "localhost";
	private static final String SERVICE_NAME = "GroupChatService";
	private static final int COM_PORT = 1009;

	ChatClientGUI chatGUI;
	private String clientServiceName;
	private String userName;
	protected ChatServerInterface serverIF;
	protected boolean connectionProblem = false;

	public ChatClient(ChatClientGUI aChatGUI, String userName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.userName = userName;
		this.clientServiceName = "ClientListenService_" + userName;
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
			Naming.rebind("rmi://" + HOSTNAME + ":" + COM_PORT + "/" + clientServiceName, this);
			serverIF = (ChatServerInterface) Naming.lookup("rmi://" + HOSTNAME + ":" + COM_PORT + "/" + SERVICE_NAME);
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(chatGUI.frame, "The server seems to be unavailable\nPlease try later",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
		} catch (NotBoundException | MalformedURLException me) {
			connectionProblem = true;
			me.printStackTrace();
		}
		if (!connectionProblem) {
			registerWithServer(details);
		}
		System.out.println("Client Listen RMI Server is running...\n");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receive a string from the chat server
	 */
	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println(message);
		chatGUI.textArea.append(message);
		chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
	}

	/**
	 * A method to update the display of users currently connected to the server
	 */
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {

		if (currentUsers.length < 2) {
			chatGUI.privateMsgButton.setEnabled(false);
		}
		chatGUI.channelPanel.remove(chatGUI.clientPanel);
		chatGUI.setClientPanel(currentUsers);
		chatGUI.clientPanel.repaint();
		chatGUI.clientPanel.revalidate();
	}
}
