package be.nikiroo.nikitv;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.ui.ImageUtilsAwt;

public class Network {
	private Downloader downloader;
	private Downloader downloaderLogo;

	public Network(Downloader downloader, Downloader downloaderLogo) {
		this.downloader = downloader;
		this.downloaderLogo = downloaderLogo;
	}

	public ImageIcon fetchLogo(ChannelData chanData, int width, int height)
			throws IOException {
		if (chanData.getLogo().isEmpty())
			return null;

		URL logoUrl = new URL(chanData.getLogo());
		InputStream in = downloaderLogo.open(logoUrl, true);
		try {
			byte[] data = IOUtils.toByteArray(in);
			ImageIcon logoOrig = new ImageIcon(data);

			Dimension target = new Dimension(width, height);
			Dimension sz = new Dimension(logoOrig.getIconWidth(),
					logoOrig.getIconHeight());
			sz = ImageUtilsAwt.scaleSize(sz, target, 1, false);
			if (sz.width > target.width)
				sz = ImageUtilsAwt.scaleSize(sz, target, 1, true);

			ImageIcon logo = new ImageIcon(logoOrig.getImage()
					.getScaledInstance(sz.width, sz.height,
							java.awt.Image.SCALE_SMOOTH));
			return logo;
		} finally {
			in.close();
		}
	}

	public List<ChannelData> getChannels(ChannelData chanData)
			throws IOException {
		List<ChannelData> subs = chanData.getChanDatas();
		if (subs == null) {
			subs = getChannels(chanData.getUrl());
			chanData.setChanDatas(subs);
		}

		return subs;
	}

	public List<ChannelData> getChannels(URL m3u) throws IOException {
		List<ChannelData> channels = new ArrayList<ChannelData>();
		Map<String, List<ChannelData>> groups = new HashMap<String, List<ChannelData>>();
		InputStreamReader isr = new InputStreamReader(downloader.open(m3u));
		try {
			BufferedReader reader = new BufferedReader(isr);
			try {
				String meta = "";
				for (String line = reader.readLine(); line != null; line = reader
						.readLine()) {
					if (line.trim().isEmpty())
						continue;
					if (line.trim().startsWith("#")) {
						meta = line.trim();
						continue;
					}

					// Name is also at end of line, in case not in
					// the meta
					String tab[] = meta.split(",");
					String defname = tab.length > 1 ? tab[tab.length - 1]
							.trim() : "";

					// Look into the metadata
					String name = getValue(meta, "tvg-name");
					String logo = getValue(meta, "tvg-logo");
					String group = getValue(meta, "group-title");
					boolean isGroup = "yes".equals(getValue(meta, "is-group"));

					if (name.isEmpty()) {
						name = defname;
						if (name.isEmpty()) {
							name = line.substring(line.indexOf('/') + 1);
							for (String ext : new String[] { ".m3u", ".m3u8",
									".mpeg", ".mpg", ".mp4" })
								if (name.endsWith(ext))
									name = name.substring(0, name.length()
											- ext.length());
						}
					}

					name = name.replaceAll(" *:", ":").trim();

					// Fix relative paths
					String base = m3u.toExternalForm();
					if (base.endsWith("/")) {
						base = base.substring(0, base.length() - 1);
					} else {
						int pos = base.lastIndexOf("/");
						base = base.substring(0, pos);
					}
					boolean relativeLine = true;
					boolean relativeLogo = true;
					for (String prefix : new String[] { "file", "http",
							"https", "ftp", "ftps" }) {
						String p = prefix + "://";
						if (line.startsWith(p))
							relativeLine = false;
						if (logo.startsWith(p))
							relativeLogo = false;
					}
					if (!line.isEmpty() && relativeLine)
						line = base + "/" + line;
					if (!logo.isEmpty() && relativeLogo)
						logo = base + "/" + logo;
					//

					ChannelData chan;
					try {
						chan = new ChannelData(name, line, logo, group, isGroup);
						if (group == null || group.isEmpty()) {
							channels.add(chan);
						} else {
							if (!groups.containsKey(group)) {
								groups.put(group, new ArrayList<ChannelData>());
							}
							groups.get(group).add(chan);
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			} finally {
				reader.close();
			}
		} finally {
			isr.close();
		}

		for (String groupName : groups.keySet()) {
			ChannelData subs = new ChannelData(groupName, null, null, null,
					true);
			subs.setChanDatas(groups.get(groupName));
			channels.add(subs);
		}

		return channels;
	}

	private String getValue(String line, String key) {
		int pos = line.indexOf(key);
		if (pos >= 0) {
			String tmp = line.substring(pos);
			int start = tmp.indexOf('"');
			if (start >= 0) {
				int stop = tmp.indexOf('"', start + 1);
				if (stop > 0) {
					return tmp.substring(start + 1, stop);
				}
			}
		}

		return "";
	}
}
