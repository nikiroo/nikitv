package be.nikiroo.nikitv;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ChannelData {
	private String name;
	private URL url;
	private String logo;

	public ChannelData(String name, String url, String logo)
			throws MalformedURLException {
		this.name = name;
		this.url = new URL(url);
		this.logo = logo == null ? "" : logo;
	}

	public String getName() {
		return name;
	}

	public URL getUrl() {
		return url;
	}

	// never null, be can be empty
	public String getLogo() {
		return logo;
	}

	/**
	 * Textual description of this channel, for DEBUG purposes only.
	 */
	@Override
	public String toString() {
		String l = logo.isEmpty() ? "no logo" : "logo: " + logo;
		return "[" + getName() + "]: " + getUrl() + " (" + l + ")";
	}
}
