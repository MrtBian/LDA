package datapreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Textread {
	DataConfig dataconfig = new DataConfig();
	String datadir = dataconfig.datadir;
	ArrayList<ArrayList<String>> docs; 
	
	public Textread(){
		docs = new ArrayList<ArrayList<String>>();
	}
	
	public void readDocs() throws IOException{// get all names of docs
		String docsPath = this.datadir;
		for(File docFile : new File(docsPath).listFiles()){
			BufferedReader reader = null;
	        try {
	            //System.out.println("以行为单位读取文件内容，一次读一整行：");
	            reader = new BufferedReader(new FileReader(docFile));
	            String tempString = null;
	            //int line = 1;
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	                //System.out.println("line " + line + ": " + tempString);
	                //line++;
	            	
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

}
