package chatroom.domain;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class ChatClientConfiguration {

	public static class Constants {
		public static final Font MEIRYO_FONT_14 = new Font("Meiryo", Font.PLAIN, 14);
		public static final Font MEIRYO_FONT_16 = new Font("Meiryo", Font.PLAIN, 16);
		public static final Border RIGHT_BLANK_BORDER = BorderFactory.createEmptyBorder(20, 10, 20, 20);// top,r,b,l
		public static final Border LEFT_BLANK_BORDER = BorderFactory.createEmptyBorder(20, 20, 20, 10);// top,r,b,l

		public static final boolean RESIZABLE = false;

		public static final int CHAT_WIDTH = 60;
		public static final int CHANNEL_PREFFERED_HEIGHT = 220;
		public static final int SPECIAL_CHANNEL_PREFFERED_HEIGHT = 200;

		public static final int MAIN_FRAME_MINIMUM_WIDHT = 600;
		public static final int MAIN_FRAME_MINIMUM_HEIGHT = 800;

		public static final String APP_TITLE = "Discord Lite ™";

		public static final String SPECIAL_CHANNEL_INFINI = "#infini";
		public static final String SPECIAL_CHANNEL_SPEAKUP = "#speak-up";
	}
}
