package chatroom;

import java.util.logging.Logger;

import chatroom.util.LookAndFeelHandler;

/**
 * Client instantiation entry point
 */
public class MainClient {

	private static final Logger log = Logger.getLogger(MainClient.class.getName());

	public static void main(String[] args) {
		try {
			LookAndFeelHandler.setLookAndFeelInfo(LookAndFeelHandler.NIMBUS_LOOK_AND_FEEL);
			ChatClientGUI chatClientGUI = new ChatClientGUI();
			chatClientGUI.initGUIContents();
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}

}
