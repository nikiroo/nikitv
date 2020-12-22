package be.nikiroo.nikitv.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import be.nikiroo.nikitv.ChannelData;
import be.nikiroo.nikitv.Network;
import be.nikiroo.utils.ui.ListModel;
import be.nikiroo.utils.ui.ListModel.Predicate;
import be.nikiroo.utils.ui.ListenerPanel;
import be.nikiroo.utils.ui.SearchBar;
import be.nikiroo.utils.ui.UIUtils;
import be.nikiroo.utils.ui.compat.JList6;
import be.nikiroo.utils.ui.compat.ListCellRenderer6;

public class ChannelList extends ListenerPanel {
	private static final long serialVersionUID = 1L;

	private Network net;

	private Map<ChannelData, Channel> chanMap = new HashMap<ChannelData, Channel>();
	private JList6<ChannelData> list;
	private ListModel<ChannelData> data;

	public ChannelList(Network net) {
		this.net = net;

		setLayout(new BorderLayout());
		setBorder(null);

		list = new JList6<ChannelData>();
		data = new ListModel<ChannelData>(list);

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					list.setSelectedIndex(index);
					fireActionPerformed("play");
				}
			}
		});

		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER
						|| e.getKeyCode() == KeyEvent.VK_ACCEPT) {
					fireActionPerformed("play");
					e.consume();
				}
				super.keyTyped(e);
			}
		});

		list.setSelectedIndex(0);
		list.setCellRenderer(generateRenderer());
		list.setVisibleRowCount(0);
		list.setLayoutOrientation(JList6.HORIZONTAL_WRAP);

		StringBuilder longString = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			longString
					.append("Some long string, which is 50 chars long itself...");
		}

		Dimension sz = null;
		try {
			sz = new Channel(new ChannelData(longString.toString(),
					"http://example.com/", "")).getPreferredSize();
		} catch (MalformedURLException e) {
			// will not happen
		}

		list.setFixedCellHeight((int) sz.getHeight());
		list.setFixedCellWidth((int) sz.getWidth());

		final SearchBar search = new SearchBar();
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String filter = search.getText().toLowerCase();
				data.filter(new Predicate<ChannelData>() {
					@Override
					public boolean test(ChannelData item) {
						return item.getName().toLowerCase().contains(filter);
					}
				});
			}
		});

		add(search, BorderLayout.NORTH);
		add(UIUtils.scroll(list, false), BorderLayout.CENTER);
	}

	public ChannelData getSelected() {
		return data.getUniqueSelectedElement();
	}

	public void setChannels(List<ChannelData> chanDatas) {
		if (chanDatas == null)
			chanDatas = new ArrayList<ChannelData>();

		chanMap.clear();
		for (ChannelData chanData : chanDatas) {
			chanMap.put(chanData, new Channel(chanData));
		}

		data.clearItems();
		data.addAllItems(chanDatas);
		data.filter(null);

		new SwingWorker<Void, Channel>() {
			@Override
			protected Void doInBackground() throws Exception {
				for (Channel chan : chanMap.values()) {
					chan.fetchLogo(net);
					publish(chan);
				}

				return null;
			}

			@Override
			protected void process(List<Channel> chunks) {
				for (Channel chan : chunks) {
					chan.invalidate();
					data.fireElementChanged(chan.getData());
				}
			}

			@Override
			protected void done() {
				ChannelList.this.invalidate();
				ChannelList.this.repaint();
			}
		}.execute();
	}

	private ListCellRenderer6<ChannelData> generateRenderer() {
		return new ListCellRenderer6<ChannelData>() {
			@Override
			public Component getListCellRendererComponent(
					JList6<ChannelData> list, ChannelData value, int index,
					boolean isSelected, boolean cellHasFocus) {

				Channel chan = chanMap.get(value);
				if (chan != null) {
					chan.setSelected(isSelected);
					chan.setHovered(data.isHovered(index));
				}

				return chan;
			}
		};
	}
}
