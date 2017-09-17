package util.text;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilities for parts of speech files
 * @author dbacon
 *
 */
public class PartsOfSpeechHelper {
    private static final Logger logger = LogManager.getLogger(PartsOfSpeech.class);
    
	public static void main(String[] args) {
		PartsOfSpeechHelper helper = null;
		String inputFile = null;		// complete path
		String outputFile = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-out")) {
				outputFile = args[++i];
			}
		}
		try {
			helper = new PartsOfSpeechHelper();
			helper.createPartsOfSpeechFile(inputFile, outputFile);
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
	}
	
	private String inputFileName = null;
	private String outputFileName = null;
	private BufferedReader inputFileReader;
	private PrintWriter outputFileWriter;
	private PartsOfSpeech partsOfSpeech = null;
	
	public PartsOfSpeechHelper() throws IOException {
		this.partsOfSpeech = PartsOfSpeech.newInstance();
	}
	
	public void createPartsOfSpeechFile(String inputFile, String outputFile)  throws IOException {
		this.inputFileName = inputFile;
		this.outputFileName = outputFile;
		inputFileReader = new BufferedReader(new FileReader(inputFile));
		if(outputFileName == null) {	// write to stdout
			outputFileWriter = new PrintWriter(new OutputStreamWriter(System.out));
			outputFileName = "stdout";
		}
		else {
			outputFileWriter = new PrintWriter(new FileWriter(outputFile, false));	// don't append
		}
		String line;
		int nwords = 0;
		int lwords = 0;
		int notFound = 0;
		String word = null;
		char tab = '\t';
		while((line = inputFileReader.readLine()) != null) {
			nwords++;
			word = line.trim();
			if(word.length() <= 1) { continue; }
			String pos = partsOfSpeech.lookup(word);
			if(pos != null) {
				lwords++;
				outputFileWriter.println(word + tab + pos);
			} 
			else {
				notFound++;
				logger.debug(word + " not found");
			}
		}
		logger.info(outputFileName + " created");
		logger.info(nwords + " words read");
		logger.info(lwords + " loaded");
		logger.info(notFound + " words not found");
		outputFileWriter.close();
		inputFileReader.close();
	}

}
