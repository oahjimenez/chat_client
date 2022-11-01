package src;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class MainClient {

	private static final String NIMBUS_LOOK_AND_FEEL = "Nimbus";

	private static final Logger log = Logger.getLogger(MainClient.class.getName());

	public static void main(String[] args) {
		setLookAndFeelInfo(NIMBUS_LOOK_AND_FEEL);
		new ChatClientGUI();
	}

	public static void setLookAndFeelInfo(String LookAndFeelName) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (LookAndFeelName.equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
}
