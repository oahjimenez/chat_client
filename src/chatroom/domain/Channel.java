package chatroom.domain;

import java.io.Serializable;
import java.util.Objects;

public class Channel implements Serializable {

	private static final long serialVersionUID = -3548424762934889766L;
	private String title;
	private boolean isSpecial;

	public Channel() {
	}

	public Channel(String title) {
		this.title = title;
	}

	public Channel(String title, boolean isSpecial) {
		this.title = title;
		this.isSpecial = isSpecial;
	}

	public String getTitle() {
		return this.title;
	}

	public void setIsSpecial(boolean isSpecial) {
		this.isSpecial = isSpecial;
	}

	public boolean isSpecial() {
		return this.isSpecial;
	}

	@Override
	public int hashCode() {
		return Objects.hash(title);
	}

	public static Channel fromTitle(String title) {
		return new Channel(title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Channel other = (Channel) obj;
		return Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "Channel [title=" + title + "]";
	}

}
