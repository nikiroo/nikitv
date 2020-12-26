package be.nikiroo.nikitv;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ChannelData {
	private String name;
	private URL url;
	private String logo;
	private String group;
	private boolean groupMode;
	private List<ChannelData> chanDatas;

	// no URL only allowed for groups
	public ChannelData(String name, String url, String logo, String group,
			boolean groupMode) throws MalformedURLException {
		this.name = name == null ? "" : name;
		this.url = (url == null && groupMode) ? null : new URL(url);
		this.logo = logo == null ? "" : logo;
		this.group = group == null ? "" : group;
		this.groupMode = groupMode;
	}

	// never null
	public String getName() {
		return name;
	}

	// null for groups
	public URL getUrl() {
		return url;
	}

	// never null, but can be empty
	public String getLogo() {
		return logo;
	}

	// the group it belongs to
	public String getGroup() {
		return group;
	}

	public boolean isGroupMode() {
		return groupMode;
	}

	// only for groupMode, can be NULL
	public List<ChannelData> getChanDatas() {
		if (!groupMode)
			return null;

		return chanDatas;
	}

	void setChanDatas(List<ChannelData> chanDatas) {
		this.chanDatas = chanDatas;
	}

	/**
	 * Textual description of this channel, for DEBUG purposes only.
	 */
	@Override
	public String toString() {
		return String.format("Channel [%s]%s %s a logo: %s", name,
				groupMode ? " (is a group)" : "", logo.isEmpty() ? "without"
						: "with", "" + url);
	}
}
