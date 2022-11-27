package chatroom.util;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Client look and feel handler
 */
public class LookAndFeelHandler {

	public static final String NIMBUS_LOOK_AND_FEEL = "Nimbus";

	public static void setLookAndFeelInfo(String LookAndFeelName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if (LookAndFeelName.equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				break;
			}
		}

	}

}
