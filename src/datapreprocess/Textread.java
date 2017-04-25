package datapreprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Textread {
	DataConfig dataconfig = new DataConfig();
	String dbpath = dataconfig.dbpath;
	ArrayList<ArrayList<String>> docs;
	ArrayList<String> stopwords;
	Connection conn;
	Statement stat;

	public Textread() {
		Stopwords sw = new Stopwords();
		stopwords = sw.stopWords;
		docs = new ArrayList<ArrayList<String>>();
		conn = getConnection();
	}

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

	public void getData() throws IOException, SQLException {// get all names of
															// docs
		try {
			stat = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sql = "select * from document";
		ResultSet rs = stat.executeQuery(sql);
		while (rs.next()) {
			// System.out.println(rs.getRow());
			String temp = rs.getString("title") + " " + rs.getString("abstract") + " " + rs.getString("keywords");
			// System.out.println(temp);
			String[] strs = temp.split("[ +-_^&*#@<>/\\\\,;.?!:()0-9'\"“”~`{}]");
			ArrayList<String> tempArray = new ArrayList<String>();
			for (String s : strs) {
				s = s.toLowerCase();
				if (!stopwords.contains(s) && s.length() > 1) {
					tempArray.add(s);
				}
			}
			docs.add(tempArray);
		}
		rs.close();
		conn.close();
	}

	public void writeData(String datapath) {
		File file = new File(datapath);
		if (file.exists() && file.isFile())
			file.delete();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
			ArrayList<ArrayList<String>> temp = docs;
			// System.out.println(temp.size());
			int numofdoc = temp.size();
			writer.write(numofdoc + "\n");
			for (ArrayList<String> arr : temp) {
				for (String s : arr) {
					writer.append(s + " ");
				}
				writer.append("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
				}
			}
		}
	}
/*
	public static void main(String args[]) throws IOException, SQLException {
		Textread textread = new Textread();
		textread.getData();
		ArrayList<ArrayList<String>> temp = textread.docs;
			
			int c=0,lc=0; for(ArrayList<String> arr:temp){ for(String s:arr){
			c++; System.out.print(s+" "); } lc++; System.out.println(); }
			System.out.println(c+" "+lc);
			
		textread.writeData("Data/data.txt");
	}
*/
}
