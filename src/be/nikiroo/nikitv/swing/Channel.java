package be.nikiroo.nikitv.swing;

import java.awt.Color;

import javax.swing.ImageIcon;

import be.nikiroo.nikitv.ChannelData;
import be.nikiroo.nikitv.Network;
import be.nikiroo.utils.ui.Item;

public class Channel extends Item {
	private static final long serialVersionUID = 1L;

	private ChannelData chanData;
	private int hOff = 0;
	private int vOff = 0;

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

	@Override
	protected int getCoverHOffset() {
		return hOff + super.getCoverHOffset();
	}

	@Override
	protected int getCoverVOffset() {
		return vOff + super.getCoverVOffset();
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
			ImageIcon img = net.fetchLogo(chanData, getCoverWidth(),
					getCoverHeight());
			hOff = (getCoverWidth() - img.getIconWidth()) / 2;
			vOff = (getCoverHeight() - img.getIconHeight()) / 2;
			setImage(img.getImage());
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
