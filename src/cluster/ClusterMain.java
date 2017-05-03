package cluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.contexts.IContextIds;

/**
 * 聚类器主类，提供主函数入口 原代码作者杨柳 代码改动 1.聚类来源不再来源文件，而是来源与参数传递。格式为 Map<Integer,
 * ArrayList<String>> 2.舍弃了预处理部分（传进来的数据已经处理过）
 */
public class ClusterMain {

	Map<Integer, ArrayList<String>> docs = new HashMap<Integer, ArrayList<String>>();
	ArrayList<Integer> ids = new ArrayList<Integer>();// 要聚类的文档标号。

	public void getDocs(String datapath) {
		File file = new File(datapath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = reader.readLine();// num of docs
			int id = 0;
			while ((tempString = reader.readLine()) != null) {
				String[] strs = tempString.split(" ");
				ArrayList<String> temp = new ArrayList<String>();
				for (String s : strs) {
					temp.add(s);
				}
				docs.put(id, temp);
				id++;
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

	/**
	 * @param ids
	 *            要聚类的文档id
	 * @param k
	 *            聚类的类别数目
	 * @return 聚类的熵值
	 * @throws IOException
	 */
	public double[] Clustermain(ArrayList<Integer> ids, int[] k) throws IOException {
		// TODO Auto-generated method stub
		// ClusterMain cMain = new ClusterMain();
		getDocs("d.txt");
		this.ids = ids;
		Map<Integer, ArrayList<String>> docs1 = new HashMap<Integer, ArrayList<String>>();
		for (Map.Entry<Integer, ArrayList<String>> entry : docs.entrySet()) {
			if (ids.contains(entry.getKey())) {
				docs1.put(entry.getKey(), entry.getValue());
			}
		}
		KmeansCluster kmeansCluster2 = new KmeansCluster();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String beginTime = sdf.format(new java.util.Date());
		System.out.println("程序开始执行时间:" + beginTime);
		// String[] terms = computeV.createTestSamples(srcDir, destDir);
		// kmeansCluster1.KmeansClusterMain(destDir, terms);
		double[] en = kmeansCluster2.KmeansClusterMain(docs1, k);
		String endTime = sdf.format(new java.util.Date());
		System.out.println("程序结束执行时间:" + endTime);
		return en;
	}
}
