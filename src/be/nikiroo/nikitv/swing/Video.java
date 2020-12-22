package be.nikiroo.nikitv.swing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import be.nikiroo.nikitv.Main;
import be.nikiroo.utils.IOUtils;

public class Video {
	static private Semaphore lock = new Semaphore(1);
	static private String prog = IOUtils.getRunningDirectory(Main.class, true)
			+ "/video";

	static public void xembed(final JFrame pane, String link)
			throws IOException {

		if (link.startsWith("file:") && !link.startsWith("file://")) {
			link = link.replaceAll("^file:", "file://");
		}

		final String title = pane.getTitle();
		String titleEmbed = "xembed_" + Math.random();
		pane.setTitle(titleEmbed);
		try {
			try {
				// Lock acquired
				lock.acquire();
			} catch (InterruptedException e) {
				throw new IOException("Technical error with the lock");
			}

			Runtime.getRuntime().exec(
					new String[] { prog, titleEmbed,
							Main.class.getName().replace(".", "-"), link });
		} catch (IOException ioe) {
			if (!new File(prog).exists()) {
				System.err
						.println("Launcher script program not found: " + prog);
				System.err.println("Please create one or use the default: "
						+ prog + ".example");
			} else {
				ioe.printStackTrace();
			}
		} finally {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								pane.setTitle(title);
							} finally {
								// MUST be run
								lock.release();
							}
						}
					});
				}
			}).start();
		}
	}
}
