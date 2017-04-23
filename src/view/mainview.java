package view;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import datapreprocess.DataConfig;
import jgibblda.LDA;

public class mainview {

	DataConfig dataconfig = new DataConfig();
	String dbpath = dataconfig.dbpath;
	Connection conn;
	Statement stat;
	private Display display;
	private Shell shell;
	private org.eclipse.swt.widgets.Label label;
	private ToolBar toolBar;
	private ToolItem ldaitem;
	private Text text;
	private Button button;
	private List list;

	public Connection getConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbpath);
			// System.out.println("connect");
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public mainview() {

		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(200, 100, 800, 600);
		shell.setText("Search");
		shell.setBackground(new Color(display, 160, 200, 180));
		shell.setLayout(new FormLayout());

		// Toolbar
		toolBar = new ToolBar(shell, SWT.FLAT);
		toolBar.setBackground(new Color(display, 255, 255, 255));
		toolBar.setForeground(display.getSystemColor(SWT.COLOR_BLACK));

		ldaitem = new ToolItem(toolBar, SWT.HORIZONTAL);
		ldaitem.setText("LDA");
		ldaitem.setToolTipText("Start LDA");
		ldaitem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// TODO Auto-generated method stub
				boolean rc = MessageDialog.openConfirm(shell, "LDA", "Start LDA?");
				// System.out.println(rc);
				if (rc) {
					try {
						MessageDialog.openInformation(shell, "LDA", "LDA is running ...\nWait a minute");
						LDA lda = new LDA();
						MessageDialog.openInformation(shell, "LDA", "DONE!");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		// Label
		label = new org.eclipse.swt.widgets.Label(shell, SWT.CENTER);
		label.setText("Literature search");
		FormData data_label = new FormData();
		data_label.height = 50;
		data_label.left = new FormAttachment(1, 4, 0);
		data_label.top = new FormAttachment(0, 50);
		label.setLayoutData(data_label);
		FontData font_label = new FontData();
		font_label.setHeight(24);
		font_label.setStyle(SWT.BOLD | SWT.ITALIC | SWT.WRAP);
		font_label.setName("Courier");
		final Font font_l = new Font(display, font_label);
		label.setFont(font_l);
		label.setForeground(display.getSystemColor(SWT.COLOR_RED));

		// TextField
		text = new Text(shell, 0);
		text.setText("please input what you want to search");
		FormData data_text = new FormData();
		data_text.height = 30;
		data_text.top = new FormAttachment(label, 30);
		data_text.left = new FormAttachment(0, 80);
		data_text.right = new FormAttachment(3, 5, 100);
		text.setLayoutData(data_text);
		text.addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
				text.setText("");
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

		});

		// Button
		button = new Button(shell, SWT.PUSH | SWT.CENTER);
		button.setText("Search");
		FormData data_button = new FormData();
		data_button.height = 30;
		data_button.top = new FormAttachment(label, 30);
		data_button.left = new FormAttachment(text, 50);
		data_button.right = new FormAttachment(100, -80);
		button.setLayoutData(data_button);
		button.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				// TODO Auto-generated method stub

				String tempstring = text.getText();
				String[] strings = tempstring.split(" ");
				ArrayList<String> words = new ArrayList<String>();
				for (String s : strings) {
					if (s.length() > 0)
						words.add(s);
				}
				// System.out.println(words.size());
				if (words.size() == 0) {
					list.removeAll();
					list.add("Please input what you want to search,separate by  blank space!");

				} else {
					list.removeAll();
					conn = getConnection();
					try {
						stat = conn.createStatement();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out.println("recommend");
					recommend rec = new recommend(words);
					System.out.println(words);
					ArrayList<Map.Entry<Integer, Double>> pdoclist = rec.re_compute();
					if (pdoclist == null) {
						list.add("Database do not have documents about this,please input again!");
					} else {
						for (int i = 0; i < pdoclist.size(); i++) {
							try {
								String sql = "select * from document where id = " + pdoclist.get(i).getKey();
								ResultSet rs = stat.executeQuery(sql);
								while (rs.next()) {
									String temp = "Title: \t" + rs.getString("title") + "\nAuthors: \t"
											+ rs.getString("author") + "\nAbstract: \t" + rs.getString("abstract")
											+ "\nKeywords: \t" + rs.getString("keywords") + "\nDoi: \t"
											+ rs.getString("doi") + "\nUrl: \t" + rs.getString("url");
									list.add(temp);
								}
								rs.close();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// list.add("Key:"+pdoclist.get(i).getKey()+"
							// Value:"+ pdoclist.get(i).getValue());
						}
						try {
							conn.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		});
		// ListView
		list = new List(shell, SWT.V_SCROLL | SWT.H_SCROLL);
		FormData data_list = new FormData();
		data_list.top = new FormAttachment(text, 30);
		data_list.left = new FormAttachment(0, 50);
		data_list.right = new FormAttachment(100, -50);
		data_list.bottom = new FormAttachment(100, -50);
		list.setLayoutData(data_list);
		/*
		 * shell.addMouseListener(new MouseListener() {
		 * 
		 * @Override public void mouseUp(MouseEvent arg0) { // TODO
		 * Auto-generated method stub
		 * text.setText("please input what you want to search"); }
		 * 
		 * @Override public void mouseDown(MouseEvent arg0) { // TODO
		 * Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void mouseDoubleClick(MouseEvent arg0) { // TODO
		 * Auto-generated method stub
		 * 
		 * } });
		 */
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		mainview mv = new mainview();
	}

}
