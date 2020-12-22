package be.nikiroo.nikitv.swing;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import be.nikiroo.nikitv.ChannelData;
import be.nikiroo.nikitv.Network;
import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.ui.Item;

public class Channel extends Item {
	private static final long serialVersionUID = 1L;

	private ChannelData chanData;

	public Channel(ChannelData chanData) {
		super(chanData.getName(), true);
		this.chanData = chanData;
	}

	@Override
	protected int getCoverHeight() {
		return 50;
	}

	@Override
	protected int getCoverWidth() {
		return 75;
	}

	public ChannelData getData() {
		return chanData;
	}

	public void fetchLogo(Network net) {
		if (chanData.getLogo().isEmpty())
			return;
		if (getImage() != null)
			return;

		try {
			setImage(net.fetchLogo(chanData, getCoverWidth(), getCoverHeight()));
		} catch (Exception e) {
			e.printStackTrace();
			setImageError(true);
		}
	}

	@Override
	protected String getMainInfoDisplay() {
		return chanData == null ? "" : chanData.getName();
	}

	@Override
	protected String getSecondaryInfoDisplay() {
		return "";
	}

	@Override
	protected Integer getStatus() {
		return null;
	}

	// UI:

	@Override
	protected Color getStatusIndicatorColor(Integer status) {
		return null;
	}

	@Override
	protected Color getOverlayColor(boolean enabled, boolean selected,
			boolean hovered) {

		Color color = new Color(255, 255, 255, 0);
		if (!enabled) {
		} else if (selected && !hovered) {
			color = new Color(80, 80, 100, 40);
		} else if (!selected && hovered) {
			color = new Color(230, 230, 255, 100);
		} else if (selected && hovered) {
			color = new Color(200, 200, 255, 100);
		}

		return color;
	}
}
