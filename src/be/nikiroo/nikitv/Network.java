package be.nikiroo.nikitv;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.IOUtils;

public class Network {
	private Downloader downloader;
	private Downloader downloaderLogo;

	public Network(Downloader downloader, Downloader downloaderLogo) {
		this.downloader = downloader;
		this.downloaderLogo = downloaderLogo;
	}

	public Image fetchLogo(ChannelData chanData, int width, int height)
			throws IOException {
		if (chanData.getLogo().isEmpty())
			return null;

		URL logoUrl = new URL(chanData.getLogo());
		InputStream in = downloaderLogo.open(logoUrl, true);
		try {
			byte[] data = IOUtils.toByteArray(in);
			ImageIcon logoOrig = new ImageIcon(data);
			ImageIcon logo = new ImageIcon(logoOrig.getImage()
					.getScaledInstance(width, height,
							java.awt.Image.SCALE_SMOOTH));
			return logo.getImage();
		} finally {
			in.close();
		}
	}

	public List<ChannelData> getChannels(ChannelData chanData)
			throws IOException {
		return getChannels(chanData.getUrl());
	}

	private List<ChannelData> getChannels(URL m3u) throws IOException {
		List<ChannelData> channels = new ArrayList<ChannelData>();
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
					String defname = tab.length > 1 ? tab[tab.length - 1] : "";

					// Look into the metadata
					String name = getValue(meta, "tvg-name");
					String logo = getValue(meta, "tvg-logo");

					if (logo == null)
						logo = "";

					if (name == null) {
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

					name = name.replaceAll(" *:", ":");

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
						chan = new ChannelData(name, line, logo);
						channels.add(chan);
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

		return null;
	}
}
