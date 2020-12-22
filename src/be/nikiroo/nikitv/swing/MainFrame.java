package be.nikiroo.nikitv.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

import be.nikiroo.nikitv.ChannelData;
import be.nikiroo.nikitv.Network;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.ui.ImageUtilsAwt;
import be.nikiroo.utils.ui.ImageUtilsAwt.Rotation;
import be.nikiroo.utils.ui.UIUtils;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel topPanel;
	private JLabel selected;
	private ChannelList list;
	private JPanel buttons;
	private JSplitPane split;
	private int splitDividerSize;

	// 0 = no, 1 = yes, 2 = yes + fullsize
	private int fs;

	public MainFrame(final List<ChannelData> sources, final Network net) {
		setTitle("Niki TV");
		setIcon();
		setLayout(new BorderLayout());

		topPanel = new JPanel(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);

		// Playing now
		JLabel playNow = new JLabel("Currently playing: ");
		selected = new JLabel("");
		playNow.setFont(selected.getFont().deriveFont(Font.BOLD));
		topPanel.add(playNow, BorderLayout.WEST);
		topPanel.add(selected, BorderLayout.CENTER);

		// Fullscreen buttons
		JPanel fspane = new JPanel();
		fspane.setLayout(new BoxLayout(fspane, BoxLayout.LINE_AXIS));
		topPanel.add(fspane, BorderLayout.EAST);

		ImageIcon helpIcon = getIcon("help.png");
		JButton help = new JButton(helpIcon);
		if (helpIcon == null)
			help.setText(" ? ");
		fspane.add(help);
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFrame helpFrame = new JFrame("About");
					InputStream in = IOUtils.openResource(MainFrame.class,
							"about.txt");
					String content = IOUtils.readSmallStream(in);
					JLabel text = new JLabel("<HTML>"
							+ content.replace("\n", "<BR>").replace(
									"\t",
									"&nbsp;&nbsp;&nbsp;&nbsp;"
											+ "&nbsp;&nbsp;&nbsp;&nbsp;")
							+ "</HTML>");
					helpFrame.add(text);
					helpFrame.setSize(600, 600);
					helpFrame.setVisible(true);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		});
		ImageIcon fullscreen1 = getIcon("fullscreen1.png");
		JButton fullscreen = new JButton(fullscreen1);
		if (fullscreen1 == null)
			fullscreen.setText("[+]");
		fspane.add(fullscreen);
		fullscreen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setFullscreen(1);
			}
		});
		ImageIcon fullscreen2 = getIcon("fullscreen2.png");
		JButton fullscreenMax = new JButton(fullscreen2);
		if (fullscreen2 == null)
			fullscreenMax.setText("[*]");
		fspane.add(fullscreenMax);
		fullscreenMax.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setFullscreen(2);
			}
		});

		// XEmbed
		Panel xembed = new Panel();
		xembed.setBackground(Color.black);

		// List of channels
		list = initList(net);

		// Split
		split = initSplit(xembed, list);
		add(split, BorderLayout.CENTER);

		// Buttons
		buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		add(buttons, BorderLayout.SOUTH);

		for (final ChannelData source : sources) {
			final JButton button = new JButton(new AbstractAction(
					source.getName()) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					fillChannels(net, source);
				}
			});
			buttons.add(button);
			new SwingWorker<ImageIcon, Void>() {
				@Override
				protected ImageIcon doInBackground() throws Exception {
					java.awt.Image img = net.fetchLogo(source, 24, 24);
					if (img != null)
						return new ImageIcon(img);
					return null;
				}

				protected void done() {
					try {
						ImageIcon icon = get();
						if (icon != null)
							button.setIcon(icon);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}.execute();
		}

		setSize(800, 600);
		setVisible(true);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeiconified(WindowEvent e) {
				setFullscreen(0);
			}
		});
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (fs == 2) // we need a way to exit fullscreen(2)
					setFullscreen(0);
			}
		});
	}

	public void setFullscreen(int fs) {
		if (fs != this.fs) {
			this.fs = fs;
			switch (fs) {
			case 2:
			case 1:
				splitDividerSize = split.getDividerSize();
				split.setDividerSize(0);
				split.setDividerLocation(1);
				split.setResizeWeight(0);
				list.setVisible(false);
				remove(buttons);
				remove(topPanel);
				break;

			case 0:
				setExtendedState(JFrame.NORMAL);
				split.setDividerSize(splitDividerSize);
				split.setDividerLocation(0.20);
				split.setResizeWeight(0.20);
				list.setVisible(true);
				add(buttons, BorderLayout.SOUTH);
				add(topPanel, BorderLayout.NORTH);
				break;
			}

			UIUtils.setFullscreenWindow(fs == 2 ? this : null);

			invalidate();
			validate();
			repaint();
		}
	}

	private void setIcon() {
		new SwingWorker<List<java.awt.Image>, Void>() {
			protected java.util.List<java.awt.Image> doInBackground()
					throws Exception {
				InputStream in = IOUtils.openResource(MainFrame.class,
						"nikitv.png");
				try {
					Image img = new Image(in);
					BufferedImage image = ImageUtilsAwt.fromImage(img,
							Rotation.NONE);
					boolean zoomSnapWidth = image.getWidth() >= image
							.getHeight();

					List<java.awt.Image> resizedImages = new ArrayList<java.awt.Image>();
					for (int size : new Integer[] { 16, 20, 64, 400 }) {
						resizedImages.add(ImageUtilsAwt.scaleImage(image,
								new Dimension(size, size), -1, zoomSnapWidth));
					}

					return resizedImages;
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				return null;
			}

			protected void done() {
				try {
					List<java.awt.Image> imgs = get();
					if (imgs != null)
						setIconImages(imgs);
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}.execute();
	}

	private JSplitPane initSplit(Component main, Component sub) {
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sub,
				main);
		split.setOneTouchExpandable(true);
		split.setResizeWeight(0.20);
		split.setContinuousLayout(true);
		split.setDividerLocation(0.25);
		split.setBorder(null);

		return split;
	}

	private ChannelList initList(final Network net) {
		final ChannelList list = new ChannelList(net);
		list.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ChannelData chanData = list.getSelected();
				try {
					Video.xembed(MainFrame.this, chanData.getUrl()
							.toExternalForm());
					selected.setText(chanData.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		return list;
	}

	// NULL country = local channels
	private void fillChannels(final Network net, final ChannelData source) {
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				List<ChannelData> chanDatas = net.getChannels(source);
				list.setChannels(chanDatas);
				return null;
			}

			protected void done() {
				MainFrame.this.invalidate();
				MainFrame.this.validate();
				MainFrame.this.repaint();
			}
		}.execute();
	}

	private ImageIcon getIcon(String name) {
		InputStream in = IOUtils.openResource(MainFrame.class, name);
		if (in != null) {
			try {
				return new ImageIcon(IOUtils.toByteArray(in));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
