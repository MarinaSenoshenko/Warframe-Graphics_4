package view.frame;

import main.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

public class Frame extends JFrame {
	private final JMenuBar menuBar;
	protected JToolBar toolBar;

	public Frame(int x, int y, String title) {
		this();
		setSize(x, y);
		setLocationByPlatform(true);
		setTitle(title);
	}

	public Frame() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch(Exception e) {
			Main.log.error(e.getMessage());
		}
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		toolBar = new JToolBar("Main toolbar");
		toolBar.setRollover(true);
		add(toolBar, BorderLayout.PAGE_START);
	}

	public JMenuItem createMenuItem(String title, String tooltip, int mnemonic, String icon,
									String actionMethod) throws SecurityException, NoSuchMethodException {
		JMenuItem menuItem = new JMenuItem(title);
		menuItem.setMnemonic(mnemonic);
		menuItem.setToolTipText(tooltip);
		menuItem.setIcon(new ImageIcon(getClass().getResource("/" + icon), title));
		final Method method = getClass().getMethod(actionMethod);
		menuItem.addActionListener(evt -> {
			try {
				method.invoke(Frame.this);
			} catch (Exception e) {
				Main.log.error(e.getMessage());
			}
		});
		return menuItem;
	}

	public JMenu createSubMenu(String title, int mnemonic) {
		JMenu menu = new JMenu(title);
		menu.setMnemonic(mnemonic);
		return menu;
	}

	public void addSubMenu(String title, int mnemonic) {
		MenuElement menuElement = getParentMenuElement(title);
		if (menuElement == null) {
			Main.log.error("Invalid Parameters");
		}
		JMenu subMenu = createSubMenu(getMenuPathName(title), mnemonic);
		if (menuElement instanceof JMenuBar) {
			((JMenuBar)menuElement).add(subMenu);
		}
		else if (menuElement instanceof JMenu) {
			((JMenu)menuElement).add(subMenu);
		}
		else if (menuElement instanceof JPopupMenu) {
			((JPopupMenu)menuElement).add(subMenu);
		}
		else {
			Main.log.error("Invalid Parameters");
		}
	}

	public void addMenuItem(String title, String tooltip, int mnemonic, String icon, String actionMethod) throws SecurityException, NoSuchMethodException {
		MenuElement menuElement = getParentMenuElement(title);
		if (menuElement == null) {
			Main.log.error("Invalid Parameters");
		}
		JMenuItem menuItem = createMenuItem(getMenuPathName(title), tooltip, mnemonic, icon, actionMethod);
		if (menuElement instanceof JMenu) {
			((JMenu)menuElement).add(menuItem);
		}
		else if (menuElement instanceof JPopupMenu) {
			((JPopupMenu)menuElement).add(menuItem);
		}
		else {
			Main.log.error("Invalid Parameters");
		}
	}

	private String getMenuPathName(String menuPath) {
		int pos = menuPath.lastIndexOf('/');
		if (pos > 0) {
			return menuPath.substring(pos + 1);
		}
		return menuPath;
	}

	private MenuElement getParentMenuElement(String menuPath) {
		int pos = menuPath.lastIndexOf('/');
		if (pos > 0) {
			return getMenuElement(menuPath.substring(0, pos));
		}
		return menuBar;
	}

	public MenuElement getMenuElement(String menuPath) {
		MenuElement menuElement = menuBar;
		for (String pathElement: menuPath.split("/")) {
			MenuElement newElement = null;
			for (MenuElement subElement: menuElement.getSubElements()) {
				if ((subElement instanceof JMenu && ((JMenu)subElement).getText().equals(pathElement))
						|| (subElement instanceof JMenuItem && ((JMenuItem)subElement).getText().equals(pathElement))) {
					if (subElement.getSubElements().length == 1 && subElement.getSubElements()[0] instanceof JPopupMenu) {
						newElement = subElement.getSubElements()[0];
					}
					else {
						newElement = subElement;
					}
					break;
				}
			}
			if (newElement == null) {
				return null;
			}
			menuElement = newElement;
		}
		return menuElement;
	}

	public JButton createToolBarButton(JMenuItem menuItem) {
		JButton button = new JButton(menuItem.getIcon());
		for (ActionListener actionListener: menuItem.getActionListeners()) {
			button.addActionListener(actionListener);
		}
		button.setToolTipText(menuItem.getToolTipText());
		return button;
	}

	public JButton createToolBarButton(String menuPath) {
		JMenuItem menuItem = (JMenuItem)getMenuElement(menuPath);
		if (menuItem == null) {
			Main.log.error("Invalid Parameters");
		}
		assert menuItem != null;
		return createToolBarButton(menuItem);
	}

	public void addToolBarButton(String menuPath) {
		toolBar.add(createToolBarButton(menuPath));
	}

	public void addToolBarSeparator() {
		toolBar.addSeparator();
	}
}
