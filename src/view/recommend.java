package view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import jgibblda.LDACmdOption;

public class recommend {
	private int ntopics, nwords, ndocs;
	private String model_path;
	private ArrayList<String> words;// 搜索词汇
	private ArrayList<Integer> wordid = new ArrayList<Integer>();// 搜索词汇对应的标号
	private Map<String, Integer> wordmap = new HashMap<String, Integer>();// 所有词汇的标号
	private double[][] t_w;
	private double[][] d_t;
	private Map<Integer, Double> pdoc = new HashMap<Integer, Double>();
	private Map<Integer, Double> ptopic = new HashMap<Integer, Double>();

	public ArrayList<Map.Entry<Integer, Double>> re_compute() {
		System.out.println(wordid);
		System.out.println(nwords + " " + ntopics + " " + ndocs);
		if (wordid.size() == 0)
			return null;
		for (int i = 0; i < ntopics; i++) {
			double temp = 0.0;
			for (int t : wordid) {
				temp += t_w[i][t];
			}
			ptopic.put(i, temp);
		}
		for (int i = 0; i < ndocs; i++) {
			double temp = 0.0;
			for (int j = 0; j < ntopics; j++) {
				temp += d_t[i][j] * ptopic.get(j);
				// System.out.println(d_t[i][j]);
			}
			pdoc.put(i, temp);
		}
		ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(pdoc.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				if ((o2.getValue() - o1.getValue()) > 0)
					return 1;
				else if ((o2.getValue() - o1.getValue()) == 0)
					return 0;
				else
					return -1;
			}
		});
		return list;
		/*
		 * Map<Integer, Double> newpdoc = new HashMap<Integer, Double>(); for
		 * (int i = 0; i < list.size(); i++) {
		 * System.out.println(list.get(i).getKey()+" "+ list.get(i).getValue());
		 * newpdoc.put(list.get(i).getKey(), list.get(i).getValue()); } return
		 * newpdoc;
		 */
	}

	public recommend(ArrayList<String> words) {
		// TODO Auto-generated constructor stub
		// System.out.println("init");
		LDACmdOption option = new LDACmdOption();
		ntopics = 0;
		nwords = 0;
		ndocs = 0;
		model_path = option.dir;
		this.words = words;// 从前端得到
		// int ntopics=0,nwords=0,ndocs=0;
		File parafile = new File(model_path + "/model-final.others");// 参数文件
		File phifile = new File(model_path + "/model-final.phi");// t-w文件
		File thetafile = new File(model_path + "/model-final.theta");// d_t文件
		File wordmapfile = new File(model_path + "/wordmap.txt");
		BufferedReader reader = null;
		try {
			// 获得词数，主题数，文档数
			// System.out.println("get");
			reader = new BufferedReader(new FileReader(parafile));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				String[] str = tempString.split("=");
				System.out.println(str[0]);
				if (str[0].equals("nwords"))
					nwords = Integer.parseInt(str[1]);
				else if (str[0].equals("ntopics"))
					ntopics = Integer.parseInt(str[1]);
				else if (str[0].equals("ndocs"))
					ndocs = Integer.parseInt(str[1]);
			}
			reader.close();

			// 获得词和标号对应
			reader = new BufferedReader(new FileReader(wordmapfile));
			tempString = reader.readLine();// 词数目
			while ((tempString = reader.readLine()) != null) {
				String[] str = tempString.split(" ");
				wordmap.put(str[0], Integer.parseInt(str[1]));
			}
			reader.close();

			// 将词转化为标号
			for (String s : words) {
				if (wordmap.containsKey(s.toLowerCase())) {
					wordid.add(wordmap.get(s.toLowerCase()));
				}
			}

			t_w = new double[ntopics][nwords];
			d_t = new double[ndocs][ntopics];
			// 获取主题——词的分布
			reader = new BufferedReader(new FileReader(phifile));
			tempString = null;
			int line = 0;
			while ((tempString = reader.readLine()) != null) {
				String[] str = tempString.split(" ");
				for (int i = 0; i < nwords; i++)
					t_w[line][i] = Double.parseDouble(str[i]);
				line++;
			}
			reader.close();

			// 获取文档——主题的分布
			reader = new BufferedReader(new FileReader(thetafile));
			tempString = null;
			line = 0;
			while ((tempString = reader.readLine()) != null) {
				String[] str = tempString.split(" ");
				for (int i = 0; i < ntopics; i++)
					d_t[line][i] = Double.parseDouble(str[i]);
				line++;
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
