package be.nikiroo.nikitv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import be.nikiroo.nikitv.swing.MainFrame;
import be.nikiroo.utils.Cache;
import be.nikiroo.utils.CacheMemory;
import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.IOUtils;

public class Main {
	public static void main(String[] args) {
		Cache cacheLogos;
		try {
			File dir = new File(IOUtils.getRunningDirectory(Main.class, true),
					"cache");
			cacheLogos = new Cache(dir, 24, 24 * 30);
		} catch (IOException e) {
			e.printStackTrace();
			cacheLogos = new CacheMemory();
		}
		Downloader downloaderLogo = new Downloader(
				"Mozilla/5.0 (X11; Linux x86_64; rv:68.0) Gecko/20100101 Firefox/68.0",
				cacheLogos);
		Downloader downloader = new Downloader(
				"Mozilla/5.0 (X11; Linux x86_64; rv:68.0) Gecko/20100101 Firefox/68.0",
				new CacheMemory());

		Network net = new Network(downloader, downloaderLogo);
		MainFrame main = new MainFrame(getSources(net, true), net);

		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static List<ChannelData> getSources(Network net,
			boolean generateDefaultIf404) {
		List<ChannelData> sources = new ArrayList<ChannelData>();

		File dir = IOUtils.getRunningDirectory(Main.class, true);
		File configFile = new File(dir, "lists/nikitv.m3u");

		try {
			String config = configFile.toURI().toURL().toExternalForm();
			for (ChannelData source : net.getChannels(new ChannelData("",
					config, ""))) {
				sources.add(source);
			}
		} catch (IOException e) {
			System.err.println("Cannot load config file: " + configFile);
			if (generateDefaultIf404 && !configFile.exists()) {
				System.err.println("Generating default config file...");
				StringBuilder content = new StringBuilder();
				content.append("#EXTM3U\n")
						.append("#EXTINF: tvg-logo=\"../images/flags/be.png\",Belgique\n") //
						.append("https://iptv-org.github.io/iptv/countries/be.m3u\n") //
						.append("#EXTINF: tvg-logo=\"../images/flags/fr.png\",France\n") //
						.append("https://iptv-org.github.io/iptv/countries/fr.m3u\n") //
				;

				try {
					IOUtils.writeSmallFile(configFile, content.toString());
					return getSources(net, false);
				} catch (IOException e2) {
					System.err.println("Failed to generate default config");
					e2.printStackTrace();
				}
			} else {
				e.printStackTrace();
			}
		}
		return sources;
	}
}
