package com.mz.utils;

import javax.swing.JOptionPane;

public class Dialog {

	public static final int NO_ICON = -1;
	public static final int ERROR = 0;
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final int QUESTION = 3;

	private String title;
	private String message;
	private Integer icon;
	private JOptionPane builtDialog = null;

	/**
	 * Creates a pop-up message with custom icon, title and message.
	 *
	 * @param title
	 *            title of the message box
	 * @param message
	 *            message to be displayed on the dialog box
	 * @param icon
	 */
	public Dialog(String title, String message, int icon) {
		this.title = title;
		this.message = message;
		this.icon = icon;
	}

	public String getTitle() {
		return this.title;
	}

	public String getMessage() {
		return this.message;
	}

	public Integer getIcon() {
		return this.icon;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setIcon(Integer icon) {
		this.icon = icon;
	}

	/**
	 * Builds a new dialog
	 *
	 * @return the built dialog
	 */
	public Dialog build() {
		this.builtDialog = new JOptionPane(this.message, this.icon, JOptionPane.OK_OPTION);
		return this;
	}

	/**
	 * Shows dialog. Use after build()
	 *
	 * @throws IllegalStateException
	 *             if the dialog hasn't been built yet
	 */
	public void show() {
		if (this.builtDialog == null)
			throw new IllegalStateException("The dialog hasn't been built yet!");
		this.builtDialog.createDialog(this.title).setVisible(true);
	}
}