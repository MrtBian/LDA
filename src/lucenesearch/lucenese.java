package lucenesearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class lucenese {

	/**
	 * @author wing
	 * 
	 * @date 2017.5.4
	 * 
	 *       基于lucene的简单搜索引擎
	 */
	Directory directory = null;
	Analyzer analyzer = null;
	Map<Integer, ArrayList<String>> docs = new HashMap<Integer, ArrayList<String>>();
	Map<Integer, String> docss = new HashMap<Integer, String>();
	private int listnum = 100;
	
	public lucenese(int listnum) {
		// TODO Auto-generated constructor stub
		this.listnum =listnum;
	}

	/**
	 * 创建索引
	 * 
	 * @param indexpath
	 *            所以文件目录
	 * @throws IOException
	 */
	public void createindex(String indexpath) throws IOException {
		analyzer = new StandardAnalyzer();
		directory = FSDirectory.open(FileSystems.getDefault().getPath(indexpath));
		File index = new File(indexpath);
		if (!index.exists()) {

			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);
			System.out.println("Num of Docs :" + docss.size());
			for (int i = 0; i < docss.size(); i++) {
				Document doc = new Document();
				String text = docss.get(i);
				doc.add(new Field("id", new String(i + ""), TextField.TYPE_STORED));
				doc.add(new Field("context", text, TextField.TYPE_STORED));
				iwriter.addDocument(doc);
			}
			iwriter.close();
		}
	}

	/**
	 * 通过索引进行关键词搜索
	 * 
	 * @param words
	 *            关键词数组
	 * @return 文档标号
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<Integer> search(ArrayList<String> words) throws IOException, ParseException {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		int len = words.size();
		Builder builder = new BooleanQuery.Builder();
		for (int i = 0; i < len; i++) {
			Term term = new Term("context", words.get(i));
			Query que = new FuzzyQuery(term);
			builder.add(que, Occur.SHOULD);

		}
		BooleanQuery query = builder.build();
		// FuzzyQuery fuzzyQuery = MultiFieldQueryParser.parse(queries, fields,
		// clauses, new StandardAnalyzer());
		ScoreDoc[] hits = isearcher.search(query, listnum).scoreDocs;
		System.out.println("Hits :" + hits.length);
		if(hits.length == 0)
			return null;
		for (int i = 0; i < hits.length; i++) {
			ids.add(hits[i].doc);
			//System.out.println(hits[i].score);
		}
		ireader.close();
		return ids;
	}

	/**
	 * close directory
	 * 
	 * @throws IOException
	 */
	public void closedirectory() throws IOException {
		directory.close();
	}

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		lucenese luse = new lucenese(100);
		luse.getDocs("d.txt");
		luse.createindex("index");
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("view");
		keys.add("computer");
		//String[] keys = { "view", "compu" };
		luse.search(keys);
		luse.closedirectory();
	}

	public void getDocs(String datapath) {
		File file = new File(datapath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = reader.readLine();// num of docs
			int id = 0;
			while ((tempString = reader.readLine()) != null) {
				docss.put(id, tempString);
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

}
