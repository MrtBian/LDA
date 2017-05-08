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
import java.util.function.DoubleToLongFunction;

import org.apache.lucene.queryparser.classic.ParseException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.source.ILineDiffer;
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

import cluster.ClusterMain;
import datapreprocess.DataConfig;
import jgibblda.LDA;
import jgibblda.LDACmdOption;
import lucenesearch.lucenese;

public class mainview {

	DataConfig dataconfig = new DataConfig();
	LDACmdOption option = new LDACmdOption();
	String dbpath = dataconfig.dbpath;
	String docspath = option.dir + "/" + option.dfile;
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
	int listnum = 500;
	int[] K = { 90, 50, 20, 10, 8, 5, 4, 3, 2 };// 聚类的类别数目

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
						long start = System.currentTimeMillis();
						LDA lda = new LDA();
						long end = System.currentTimeMillis();
						MessageDialog.openInformation(shell, "LDA", "DONE!\nTime: " + (end - start) / 1000.0 + "s");
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
		data_text.right = new FormAttachment(5, 8, 100);
		text.setLayoutData(data_text);
		text.addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
				if (text.getText().equals("please input what you want to search")) {
					text.setText("");
				}
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
				String[] strings = tempstring.split("[^a-zA-Z]");
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
					long start = System.currentTimeMillis();
					list.removeAll();
					conn = getConnection();
					try {
						stat = conn.createStatement();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out.println("recommend");
					// LDA search
					recommend rec = new recommend(words);
					//System.out.println(words);
					ArrayList<Map.Entry<Integer, Double>> pdoclist = rec.re_compute();
					// lucene search
					ArrayList<Integer> Lucenearr = null;
					try {

						lucenese luse = new lucenese(listnum);
						luse.getDocs("d.txt");
						luse.createindex("index");
						Lucenearr = luse.search(words);
						if (Lucenearr == null)
							System.out.println("err");
						// System.out.println(Lucenearr.size());
						luse.closedirectory();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// 如果返回为0
					if (pdoclist == null) {
						list.add("Database do not have documents about this,please input again!");
					} else {
						ArrayList<Integer> LDAarr = new ArrayList<Integer>();
						for (int i = 0; i < listnum; i++) {
							if(i<5){
								System.out.println(pdoclist.get(i).getValue());
							}
							LDAarr.add(pdoclist.get(i).getKey());
						}
						
						ClusterMain cMain = new ClusterMain(docspath);
						double[][] LDA = null;
						double[][] Lucene = null;
						try {
							LDA = cMain.Clustermain(LDAarr, K);
							Lucene = cMain.Clustermain(Lucenearr, K);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						for (int i = 0; i < K.length; i++) {
							list.add("K : \t" + K[i]);
							list.add("LDA : \t" + LDA[0][i] + " " + LDA[1][i]);
							list.add("Lucene : \t" + Lucene[0][i] + " " + Lucene[1][i]);
						}
						long end = System.currentTimeMillis();
						list.add("Time: " + (end - start) / 1000.0 + "s");
						list.add("SEARCH RESULT");
						list.add("=================================================================="
								+ "=================================================================="
								+ "==================================================================");
						for (int i = 0; i < listnum; i++) {
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
