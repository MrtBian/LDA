package cluster;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Kmeans聚类算法的实现类 算法结束条件:当每个点最近的聚类中心点就是它所属的聚类中心点时，算法结束
 *
 */

public class KmeansCluster {
	Map<Integer, Map<String, Double>> allTestSampleMap;
	Map<Integer, Integer> KmeansClusterResult = new TreeMap<Integer, Integer>();
	Map<Integer, Map<String, Double>> meansMap;// K个中心点
	Integer[] testSampleNames;
	Map<Integer, Vector<Integer>> clusterMember = new TreeMap<Integer, Vector<Integer>>();// 记录每个聚类的成员点序号

	/**
	 * Kmeans算法主过程
	 * 
	 * @param Map<String,
	 *            Map<String, Double>> allTestSampleMap 聚类算法测试样本map
	 * @param int
	 *            K 聚类的数量
	 * @return Map<String,Integer> 聚类的结果 即<文件名，聚类完成后所属的类别标号>
	 * @throws IOException
	 */
	private Map<Integer, Integer> doProcess(Map<Integer, Map<String, Double>> allTestSampleMap, int K) {
		// TODO Auto-generated method stub
		// 0、首先获取allTestSampleMap所有文件ID顺序组成的数组
		testSampleNames = new Integer[allTestSampleMap.size()];
		int count = 0, tsLength = allTestSampleMap.size();
		Set<Entry<Integer, Map<String, Double>>> allTestSampeleMapSet = allTestSampleMap.entrySet();
		for (Iterator<Entry<Integer, Map<String, Double>>> it = allTestSampeleMapSet.iterator(); it.hasNext();) {
			Entry<Integer, Map<String, Double>> me = it.next();
			testSampleNames[count++] = me.getKey();
		}
		// 1、初始点的选择算法是随机选择或者是均匀分开选择，这里采用后者
		meansMap = getInitPoint(allTestSampleMap, K);// 保存K个中心点
		double[][] distance = new double[tsLength][K];// distance[i][j]记录点i到聚类中心j的距离
		// 2、初始化K个聚类
		int[] assignMeans = new int[tsLength];// 记录所有点属于的聚类序号，初始化全部为0

		Vector<Integer> mem = new Vector<Integer>();
		int iterNum = 0;// 迭代次数
		while (true) {
			// System.out.println("Iteration No." + (iterNum++) +
			// "----------------------");
			// 3、计算每个点和每个聚类中心的距离
			for (int i = 0; i < tsLength; i++) {
				for (int j = 0; j < K; j++) {
					distance[i][j] = getDistance(allTestSampleMap.get(testSampleNames[i]), meansMap.get(j));
				}
			}
			// 4、找出每个点最近的聚类中心
			int[] nearestMeans = new int[tsLength];
			for (int i = 0; i < tsLength; i++) {
				nearestMeans[i] = findNearestMeans(distance, i);
			}
			// 5、判断当前所有点属于的聚类序号是否已经全部是其离得最近的聚类，如果是或者达到最大的迭代次数，那么结束算法
			int okCount = 0;
			for (int i = 0; i < tsLength; i++) {
				if (nearestMeans[i] == assignMeans[i])
					okCount++;
			}
			// System.out.println("okCount = " + okCount);
			if (okCount == tsLength || iterNum >= 10)
				break;
			/*
			 * if(iterNum >= 5) break;
			 */
			// 6、如果前面条件不满足，那么需要重新聚类再进行一次迭代，需要修改每个聚类的成员和每个点属于的聚类信息
			clusterMember.clear();
			for (int i = 0; i < tsLength; i++) {
				assignMeans[i] = nearestMeans[i];
				if (clusterMember.containsKey(nearestMeans[i])) {
					clusterMember.get(nearestMeans[i]).add(i);
				} else {
					mem.clear();
					mem.add(i);
					Vector<Integer> tempMem = new Vector<Integer>();
					tempMem.addAll(mem);
					clusterMember.put(nearestMeans[i], tempMem);
				}
			}
			// 7、重新计算每个聚类的中心点!
			for (int i = 0; i < K; i++) {
				if (!clusterMember.containsKey(i)) {// 注意kmeans可能产生空聚类
					continue;
				}
				Map<String, Double> newMean = computeNewMean(clusterMember.get(i), allTestSampleMap, testSampleNames);
				Map<String, Double> tempMean = new TreeMap<String, Double>();
				tempMean.putAll(newMean);
				meansMap.put(i, tempMean);
			}
			iterNum++;
		}
		// 8、形成聚类结果并且返回
		Map<Integer, Integer> resMap = new TreeMap<Integer, Integer>();
		for (int i = 0; i < tsLength; i++) {
			resMap.put(testSampleNames[i], assignMeans[i]);
		}
		return resMap;
	}

	/**
	 * 计算当前聚类新的中心，采用向量平均
	 * 
	 * @param clusterM
	 *            该点到所有聚类中心的距离
	 * @param allTestSampleMap
	 *            所有测试样例的<文件名，向量>构成的map
	 * @param testSampleNames
	 *            所有测试样例文件名构成的数组
	 * @return Map<String, Double> 新的聚类中心的向量
	 * @throws IOException
	 */
	private Map<String, Double> computeNewMean(Vector<Integer> clusterM,
			Map<Integer, Map<String, Double>> allTestSampleMap, Integer[] testSampleNames) {
		// TODO Auto-generated method stub
		double memberNum = (double) clusterM.size();
		Map<String, Double> newMeanMap = new TreeMap<String, Double>();
		Map<String, Double> currentMemMap = new TreeMap<String, Double>();
		for (Iterator<Integer> it = clusterM.iterator(); it.hasNext();) {
			int me = it.next();
			currentMemMap = allTestSampleMap.get(testSampleNames[me]);
			Set<Map.Entry<String, Double>> currentMemMapSet = currentMemMap.entrySet();
			for (Iterator<Map.Entry<String, Double>> jt = currentMemMapSet.iterator(); jt.hasNext();) {
				Map.Entry<String, Double> ne = jt.next();
				if (newMeanMap.containsKey(ne.getKey())) {
					newMeanMap.put(ne.getKey(), newMeanMap.get(ne.getKey()) + ne.getValue());
				} else {
					newMeanMap.put(ne.getKey(), ne.getValue());
				}
			}
		}

		Set<Map.Entry<String, Double>> newMeanMapSet = newMeanMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> jt = newMeanMapSet.iterator(); jt.hasNext();) {
			Map.Entry<String, Double> ne = jt.next();
			newMeanMap.put(ne.getKey(), newMeanMap.get(ne.getKey()) / memberNum);
		}
		return newMeanMap;
	}

	/**
	 * 找出距离当前点最近的聚类中心
	 * 
	 * @param double[][]
	 *            点到所有聚类中心的距离
	 * @return i 最近的聚类中心的序 号
	 * @throws IOException
	 */
	private int findNearestMeans(double[][] distance, int m) {
		// TODO Auto-generated method stub
		double minDist = 10;
		int j = 0;
		for (int i = 0; i < distance[m].length; i++) {
			if (distance[m][i] < minDist) {
				minDist = distance[m][i];
				j = i;
			}
		}
		return j;
	}

	/**
	 * 计算两个点的距离
	 * 
	 * @param map1
	 *            点1的向量map
	 * @param map2
	 *            点2的向量map
	 * @return double 两个点的欧式距离
	 */
	private double getDistance(Map<String, Double> map1, Map<String, Double> map2) {
		// TODO Auto-generated method stub
		return 1 - computeSim(map1, map2);
	}

	/**
	 * 计算两个文本的相似度
	 * 
	 * @param testWordTFMap
	 *            文本1的<单词,词频>向量
	 * @param trainWordTFMap
	 *            文本2<单词,词频>向量
	 * @return Double 向量之间的相似度 以向量夹角余弦计算
	 * @throws IOException
	 */
	private double computeSim(Map<String, Double> testWordTFMap, Map<String, Double> trainWordTFMap) {
		// TODO Auto-generated method stub
		double mul = 0, testAbs = 0, trainAbs = 0;
		Set<Map.Entry<String, Double>> testWordTFMapSet = testWordTFMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> it = testWordTFMapSet.iterator(); it.hasNext();) {
			Map.Entry<String, Double> me = it.next();
			if (trainWordTFMap.containsKey(me.getKey())) {
				mul += me.getValue() * trainWordTFMap.get(me.getKey());
			}
			testAbs += me.getValue() * me.getValue();
		}
		testAbs = Math.sqrt(testAbs);

		Set<Map.Entry<String, Double>> trainWordTFMapSet = trainWordTFMap.entrySet();
		for (Iterator<Map.Entry<String, Double>> it = trainWordTFMapSet.iterator(); it.hasNext();) {
			Map.Entry<String, Double> me = it.next();
			trainAbs += me.getValue() * me.getValue();
		}
		trainAbs = Math.sqrt(trainAbs);
		return mul / (testAbs * trainAbs);
	}

	/**
	 * 获取kmeans算法迭代的初始点
	 * 
	 * @param k
	 *            聚类的数量
	 * @param Map<String,
	 *            Map<String, Double>> allTestSampleMap 所有测试样例的<文件名，向量>构成的map
	 * @return Map<Integer, Map<String, Double>> 初始中心点的Map
	 * @throws IOException
	 */
	private Map<Integer, Map<String, Double>> getInitPoint(Map<Integer, Map<String, Double>> allTestSampleMap, int K) {
		// TODO Auto-generated method stub
		int count = 0, i = 0;
		Map<Integer, Map<String, Double>> meansMap = new TreeMap<Integer, Map<String, Double>>();// 保存K个聚类中心点向量
		// System.out.println("本次聚类的初始点对应的文件为：");
		Set<Entry<Integer, Map<String, Double>>> allTestSampleMapSet = allTestSampleMap.entrySet();
		for (Iterator<Entry<Integer, Map<String, Double>>> it = allTestSampleMapSet.iterator(); it.hasNext();) {
			Map.Entry<Integer, Map<String, Double>> me = it.next();
			if (count == i * allTestSampleMapSet.size() / K) {
				meansMap.put(i, me.getValue());
				// System.out.println(me.getKey() + " map size is " +
				// me.getValue().size());
				i++;
			}
			count++;
		}
		return meansMap;
	}

	/**
	 * 输出聚类结果到文件中
	 * 
	 * @param kmeansClusterResultFile
	 *            输出文件目录
	 * @param kmeansClusterResult
	 *            聚类结果
	 * @throws IOException
	 */
	private void printClusterResult(Map<Integer, Integer> kmeansClusterResult, String kmeansClusterResultFile)
			throws IOException {
		// TODO Auto-generated method stub
		FileWriter resWriter = new FileWriter(kmeansClusterResultFile);
		Set<Entry<Integer, Integer>> kmeansClusterResultSet = kmeansClusterResult.entrySet();
		for (Iterator<Entry<Integer, Integer>> it = kmeansClusterResultSet.iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> me = it.next();
			resWriter.append(me.getKey() + " " + me.getValue() + "\n");
		}
		resWriter.flush();
		resWriter.close();
	}

	/**
	 * 计算类别C的类内平均距离
	 * 
	 * @param c
	 *            类
	 * @return 类内平均距离
	 */
	private double avg(int c) {
		int num = clusterMember.get(c).size();
		double sumdist = 0.0;
		Vector<Integer> vector = clusterMember.get(c);
		
		for (int i = 0; i < num; i++) {
			for (int j = i + 1; j < num; j++) {
				sumdist += getDistance(allTestSampleMap.get(testSampleNames[vector.get(i)]),
						allTestSampleMap.get(testSampleNames[vector.get(j)]));
			}
		}
		return sumdist * 2 / ((num - 1) * num);
	}

	/**
	 * 计算类内最大距离
	 * 
	 * @param c
	 * @return
	 */
	private double diam(int c) {
		int num = clusterMember.get(c).size();
		double maxdist = 0.0;
		Vector<Integer> vector = clusterMember.get(c);
		if (vector == null)// 空聚类
			return 0.0;
		for (int i = 0; i < num; i++) {
			for (int j = i + 1; j < num; j++) {
				double temp = getDistance(allTestSampleMap.get(testSampleNames[vector.get(i)]),
						allTestSampleMap.get(testSampleNames[vector.get(j)]));
				if (temp > maxdist)
					maxdist = temp;
			}
		}
		return maxdist;
	}

	/**
	 * 计算两类间最近的距离
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	private double dmin(int c1, int c2) {
		int num1 = clusterMember.get(c1).size();
		int num2 = clusterMember.get(c2).size();
		double mindist = 10000.0;
		Vector<Integer> vector1 = clusterMember.get(c1);
		Vector<Integer> vector2 = clusterMember.get(c2);

		for (int i = 0; i < num1; i++) {
			for (int j = 0; j < num2; j++) {
				double temp = getDistance(allTestSampleMap.get(testSampleNames[vector1.get(i)]),
						allTestSampleMap.get(testSampleNames[vector2.get(j)]));
				if (temp < mindist)
					mindist = temp;
			}
		}
		return mindist;
	}

	/**
	 * 计算两个类的中心距离
	 * 
	 * @param c1
	 * @param c2
	 * @return c1和c2的中心之间的距离
	 */
	private double dcen(int c1, int c2) {
		return getDistance(meansMap.get(c1), meansMap.get(c2));
	}
	/**
	 * 计算DI
	 * @return
	 */
	private double evaluateDI() {
		int K = clusterMember.size();// num of class
		double maxdiam = 0.0;
		for (int i = 0; i < K; i++) {
			double diam = diam(i);
			if (diam > maxdiam)
				maxdiam = diam;
		}
		double min = 10000.0;
		for (int i = 0; i < K; i++) {
			double min1 = 10000.0;
			for (int j = 0; j < K; j++) {
				if (j == i)
					continue;
				double temp = dmin(i, j) / maxdiam;
				if (temp < min1)
					min1 = temp;
			}
			if (min1 < min)
				min = min1;
		}
		return min;
	}

	/**
	 * 
	 * @return 聚类评价指标DBI
	 */
	private double evaluateDBI() {
		int K = clusterMember.size();// num of class
		for (int i = 0; i < K; i++) {
			if (!clusterMember.containsKey(i)) {// 注意kmeans可能产生空聚类
				continue;
			}
			Map<String, Double> newMean = computeNewMean(clusterMember.get(i), allTestSampleMap, testSampleNames);
			Map<String, Double> tempMean = new TreeMap<String, Double>();
			tempMean.putAll(newMean);
			meansMap.put(i, tempMean);
		}
		double sum = 0.0;
		for (int i = 0; i < K; i++) {
			double max = 0.0;
			for (int j = 0; j < K; j++) {
				if (j == i)
					continue;
				double temp = (avg(i) + avg(j)) / dcen(i, j);
				if (temp > max)
					max = temp;
			}
			sum += max;
		}
		//System.out.println(sum);
		return sum / K;
	}

	public double[][] KmeansClusterMain(Map<Integer, ArrayList<String>> docs, int[] k2) throws IOException {
		// 首先计算文档TF-IDF向量，保存为Map<String,Map<String,Double>>
		// 即为Map<文件名，Map<特征词，TF-IDF值>>
		ComputeWordsVector computeV = new ComputeWordsVector();
		// int[] K = {5,10};
		int[] K = k2;
		double[][] Entropy = new double[2][k2.length];
		allTestSampleMap = computeV.computeTFMultiIDF(docs);
		System.out.println(allTestSampleMap.size());
		for (int i = 0; i < K.length; i++) {
			System.out.print("开始聚类，聚成" + K[i] + "类.");
			// String KmeansClusterResultFile = "KmeansClusterResult/";
			// Map<Integer, Integer> KmeansClusterResult = new TreeMap<Integer,
			// Integer>();
			KmeansClusterResult = doProcess(allTestSampleMap, K[i]);
			// KmeansClusterResultFile += K[i];
			// printClusterResult(KmeansClusterResult, KmeansClusterResultFile);
			// Entropy[i] = computeV.evaluateClusterRes(KmeansClusterResultFile,
			// K[i]);
			Entropy[0][i] = evaluateDBI();
			Entropy[1][i] = evaluateDI();
			// System.out.println("The Entropy for this Cluster is " +
			// Entropy[i]);
			System.out.print("DBI=" + evaluateDBI());
			System.out.println(" DI=" + evaluateDI());
		}
		return Entropy;
	}
}
