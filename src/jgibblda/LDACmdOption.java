package jgibblda;

import org.kohsuke.args4j.*;

public class LDACmdOption {

	@Option(name = "-est", usage = "Specify whether we want to estimate model from scratch")
	public boolean est = true;

	@Option(name = "-estc", usage = "Specify whether we want to continue the last estimation")
	public boolean estc = false;

	@Option(name = "-inf", usage = "Specify whether we want to do inference")
	public boolean inf = true;

	@Option(name = "-dir", usage = "Specify directory")
	public String dir = "models/casestudy_1";

	@Option(name = "-dfile", usage = "Specify data file")
	public String dfile = "data.txt";

	@Option(name = "-model", usage = "Specify the model name")
	public String modelName = "";

	@Option(name = "-alpha", usage = "Specify alpha")
	public double alpha = 0.1;

	@Option(name = "-beta", usage = "Specify beta")
	public double beta = 0.02;

	@Option(name = "-ntopics", usage = "Specify the number of topics")
	public int K = 10;

	@Option(name = "-niters", usage = "Specify the number of iterations")
	public int niters = 300;

	@Option(name = "-savestep", usage = "Specify the number of steps to save the model since the last save")
	public int savestep = 100;

	@Option(name = "-twords", usage = "Specify the number of most likely words to be printed for each topic")
	public int twords = 20;

	@Option(name = "-withrawdata", usage = "Specify whether we include raw data in the input")
	public boolean withrawdata = false;

	@Option(name = "-wordmap", usage = "Specify the wordmap file")
	public String wordMapFileName = "wordmap.txt";
}
