package com.orange.util;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import com.orange.ui.component.custom.CustomJPanel;

public class JTabbedPaneCloseButton extends JTabbedPane {

	/* Button */
	private class CloseButtonTab extends CustomJPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CloseButtonTab(final Component tab, String title, Icon icon) {
			setOpaque(false);
			FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 3, 3);
			setLayout(flowLayout);
			JLabel jLabel = new JLabel(title);
			jLabel.setIcon(icon);
			add(jLabel);
			
			JButton button = new JButton(new ImageIcon(CommonUtils.setSizeImage("/resources/cancel.png", 15, 15)));
			button.setMargin(new Insets(0, 0, 0, 0));
			button.addMouseListener(new CloseListener(tab));
			add(button);
		}
	}
	/* ClickListener */
	public class CloseListener extends MouseAdapter {
		private Component tab;

		public CloseListener(Component tab) {
			this.tab = tab;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() instanceof JButton) {
				JButton clickedButton = (JButton) e.getSource();
				JTabbedPane tabbedPane = (JTabbedPane) clickedButton
						.getParent().getParent().getParent();

					
				tabPath.remove(tabbedPane.getToolTipTextAt(tabbedPane
						.getSelectedIndex()));
				tabbedPane.remove(tab);
			}

		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LinkedHashSet<String> tabPath = new LinkedHashSet<String>();

	public JTabbedPaneCloseButton() {
		super();
	}

	/* Override Addtab in order to add the close Button everytime */
	@Override
	public void addTab(String title, Icon icon, Component component, String tip) {

		if (!tabPath.contains(tip)) 
		{
			tabPath.add(tip);
			super.addTab(title, icon, component, tip);
			int count = this.getTabCount() - 1;
			setTabComponentAt(count, new CloseButtonTab(component, title, icon));
			// super.setSelectedIndex(this.getTabCount()-1);
		}

	}

}