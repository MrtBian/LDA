package datapreprocess;

import datapreprocess.DataConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Stopwords {
	ArrayList<String> stopWords;
	DataConfig dataconfig = new DataConfig();
	String stopdir = dataconfig.stopdir;

	Stopwords() {
		stopWords = new ArrayList<String>();
		File file = new File(stopdir);
		BufferedReader reader = null;
		try {
			// System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				stopWords.add(tempString);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}
}
