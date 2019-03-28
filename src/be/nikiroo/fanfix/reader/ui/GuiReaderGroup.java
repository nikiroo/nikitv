package be.nikiroo.fanfix.reader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import be.nikiroo.fanfix.bundles.StringIdGui;
import be.nikiroo.fanfix.reader.ui.GuiReaderBook.BookActionListener;
import be.nikiroo.utils.ui.WrapLayout;

/**
 * A group of {@link GuiReaderBook}s for display.
 * 
 * @author niki
 */
public class GuiReaderGroup extends JPanel {
	private static final long serialVersionUID = 1L;
	private BookActionListener action;
	private Color backgroundColor;
	private GuiReader reader;
	private List<GuiReaderBookInfo> infos;
	private List<GuiReaderBook> books;
	private JPanel pane;
	private boolean words; // words or authors (secondary info on books)
	private int itemsPerLine;

	/**
	 * Create a new {@link GuiReaderGroup}.
	 * 
	 * @param reader
	 *            the {@link GuiReaderBook} used to probe some information about
	 *            the stories
	 * @param title
	 *            the title of this group
	 * @param backgroundColor
	 *            the background colour to use (or NULL for default)
	 */
	public GuiReaderGroup(GuiReader reader, String title, Color backgroundColor) {
		this.reader = reader;
		this.backgroundColor = backgroundColor;

		this.pane = new JPanel();

		pane.setLayout(new WrapLayout(WrapLayout.LEADING, 5, 5));
		if (backgroundColor != null) {
			pane.setBackground(backgroundColor);
			setBackground(backgroundColor);
		}

		setLayout(new BorderLayout(0, 10));

		// Make it focusable:
		setFocusable(true);
		setEnabled(true);
		setVisible(true);

		add(pane, BorderLayout.CENTER);

		if (title != null) {
			if (title.isEmpty()) {
				title = GuiReader.trans(StringIdGui.MENU_AUTHORS_UNKNOWN);
			}

			JLabel label = new JLabel();
			label.setText(String.format("<html>"
					+ "<body style='text-align: center; color: gray;'><br><b>"
					+ "%s" + "</b></body>" + "</html>", title));
			label.setHorizontalAlignment(JLabel.CENTER);
			add(label, BorderLayout.NORTH);
		}

		// Compute the number of items per line at each resize
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				computeItemsPerLine();
			}
		});
		computeItemsPerLine();

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				onKeyPressed(e);
			}

			@Override
			public void keyTyped(KeyEvent e) {
				onKeyTyped(e);
			}
		});

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (getSelectedBookIndex() < 0) {
					setSelectedBook(0, true);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				setBackground(null);
				setSelectedBook(-1, false);
			}
		});
	}

	/**
	 * Compute how many items can fit in a line so UP and DOWN can be used to go
	 * up/down one line at a time.
	 */
	private void computeItemsPerLine() {
		// TODO
		itemsPerLine = 5;
	}

	/**
	 * Set the {@link ActionListener} that will be fired on each
	 * {@link GuiReaderBook} action.
	 * 
	 * @param action
	 *            the action
	 */
	public void setActionListener(BookActionListener action) {
		this.action = action;
		refreshBooks(infos, words);
	}

	/**
	 * Refresh the list of {@link GuiReaderBook}s displayed in the control.
	 * 
	 * @param infos
	 *            the new list of infos
	 * @param seeWordcount
	 *            TRUE to see word counts, FALSE to see authors
	 */
	public void refreshBooks(List<GuiReaderBookInfo> infos, boolean seeWordcount) {
		this.infos = infos;
		refreshBooks(seeWordcount);
	}

	/**
	 * Refresh the list of {@link GuiReaderBook}s displayed in the control.
	 * <p>
	 * Will not change the current stories.
	 * 
	 * @param seeWordcount
	 *            TRUE to see word counts, FALSE to see authors
	 */
	public void refreshBooks(boolean seeWordcount) {
		this.words = seeWordcount;

		books = new ArrayList<GuiReaderBook>();
		invalidate();
		pane.invalidate();
		pane.removeAll();

		if (infos != null) {
			for (GuiReaderBookInfo info : infos) {
				boolean isCached = false;
				if (info.getMeta() != null) {
					isCached = reader.isCached(info.getMeta().getLuid());
				}

				GuiReaderBook book = new GuiReaderBook(reader, info, isCached,
						words);
				if (backgroundColor != null) {
					book.setBackground(backgroundColor);
				}

				books.add(book);

				book.addActionListener(new BookActionListener() {
					@Override
					public void select(GuiReaderBook book) {
						GuiReaderGroup.this.requestFocusInWindow();
						for (GuiReaderBook abook : books) {
							abook.setSelected(abook == book);
						}
					}

					@Override
					public void popupRequested(GuiReaderBook book,
							Component target, int x, int y) {
					}

					@Override
					public void action(GuiReaderBook book) {
					}
				});

				if (action != null) {
					book.addActionListener(action);
				}

				pane.add(book);
			}
		}

		pane.validate();
		pane.repaint();
		validate();
		repaint();
	}

	/**
	 * Enables or disables this component, depending on the value of the
	 * parameter <code>b</code>. An enabled component can respond to user input
	 * and generate events. Components are enabled initially by default.
	 * <p>
	 * Disabling this component will also affect its children.
	 * 
	 * @param b
	 *            If <code>true</code>, this component is enabled; otherwise
	 *            this component is disabled
	 */
	@Override
	public void setEnabled(boolean b) {
		if (books != null) {
			for (GuiReaderBook book : books) {
				book.setEnabled(b);
				book.repaint();
			}
		}

		pane.setEnabled(b);
		super.setEnabled(b);
		repaint();
	}

	/**
	 * Return the index of the currently selected book if any, -1 if none.
	 * 
	 * @return the index or -1
	 */
	private int getSelectedBookIndex() {
		int index = -1;
		for (int i = 0; i < books.size(); i++) {
			if (books.get(i).isSelected()) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 * Select the given book, or unselect all items.
	 * 
	 * @param index
	 *            the index of the book to select, can be outside the bounds
	 *            (either all the items will be unselected or the first or last
	 *            book will then be selected, see <tt>forceRange>/tt>)
	 * @param forceRange
	 *            TRUE to constraint the index to the first/last element, FALSE
	 *            to unselect when outside the range
	 */
	private void setSelectedBook(int index, boolean forceRange) {
		int previousIndex = getSelectedBookIndex();

		if (index >= books.size()) {
			if (forceRange) {
				index = books.size() - 1;
			} else {
				index = -1;
			}
		}

		if (index < 0 && forceRange) {
			index = 0;
		}

		if (previousIndex >= 0) {
			books.get(previousIndex).setSelected(false);
		}

		if (index >= 0) {
			books.get(index).setSelected(true);
		}
	}

	/**
	 * The action to execute when a key is typed.
	 * 
	 * @param e
	 *            the key event
	 */
	private void onKeyTyped(KeyEvent e) {
		boolean consumed = false;
		boolean action = e.getKeyChar() == '\n';
		boolean popup = e.getKeyChar() == ' ';
		if (action || popup) {
			consumed = true;

			int index = getSelectedBookIndex();
			if (index >= 0) {
				GuiReaderBook book = books.get(index);
				if (action) {
					book.action();
				} else if (popup) {
					book.popup(book, book.getWidth() / 2, book.getHeight() / 2);
				}
			}
		}

		if (consumed) {
			e.consume();
		}
	}

	/**
	 * The action to execute when a key is pressed.
	 * 
	 * @param e
	 *            the key event
	 */
	private void onKeyPressed(KeyEvent e) {
		boolean consumed = false;
		if (e.isActionKey()) {
			int offset = 0;
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				offset = -1;
				break;
			case KeyEvent.VK_RIGHT:
				offset = 1;
				break;
			case KeyEvent.VK_UP:
				offset = -itemsPerLine;
				break;
			case KeyEvent.VK_DOWN:
				offset = itemsPerLine;
				break;
			}

			if (offset != 0) {
				consumed = true;

				int previousIndex = getSelectedBookIndex();
				if (previousIndex >= 0) {
					setSelectedBook(previousIndex + offset, true);
				}
			}
		}

		if (consumed) {
			e.consume();
		}
	}
}
