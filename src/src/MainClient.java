package src;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class MainClient {

	public static void main(String[] args) {
		setLookAndFeelInfo("Nimbus");
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
			e.getStackTrace();
		}
	}

}
