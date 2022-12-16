package chatroom.util;

/**
 * Client constants and messages
 */
public class Constants {

	public static final String NEW_LINE = System.lineSeparator();
	public static final String SINGLE_SPACE = " ";
	public static final String EMPTY_STRING = "";
	public static final String HYPHEN = "-";
	
	public static class Messages {

		public static final String WELCOME_MESSAGE = "Welcome enter your name and press Start to begin\n";
		public static final String INFINI_MESSAGE = "Write a number starting from number below up to infinity. Make sure it is not repeated within this chat!\n";
		public static final String SPEAK_UP_MESSAGE = "Eager to share an important message? Take the mic and give a shout out! Nobody will interrump you until your message is sent.\n";
		public static final String LOGOUT_MESSAGE = "Bye all, I am leaving";
		public static final String CHANNEL_BEFORE_LOGIN_MESSAGE = "Login to get started";
		
		public static final String SERVER_UNAVAILABLE_MESSAGE = "The server seems to be unavailable\nPlease try later";
		public static final String CONNECTION_PROBLEM_MESSAGE = "Connection problem";
		public static final Object USERNAME_ALREADY_TAKEN_MESSAGE = "This username is already used!";

	}

}
