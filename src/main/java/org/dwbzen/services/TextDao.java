package org.dwbzen.services;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TextDao {

	public TextDao() {
		
	}
	
	static String mysticSeerFile = "/reference/pos/mysticSeer.txt";
	static String madlibFile = "/reference/pos/madLib.txt";
	static String bandPatternsFile = "/reference/pos/bandNamePatterns.txt";
	static String insultPatternsFile = "/reference/pos/insultPatterns.txt";

	public static String[] bandPatterns = {
			"A{1,2}[N|h|p]", 
			"[M|F|N](and)(the)A?p",
			"NV(and)G",
			"[M|F](and)(the)p",
			"A?Gp",
			"(The)A?p",
			"(A)N[(in)|(with)|(around)|(without)]A?p",
			"A?Gp",
			"N{1,2}p",
			"[N|p]G[N|p]"
	};
	
	public static String[] insultPatterns = {
			"!(Where did you get that)A{1,3}[b|B](?)"
	};
	
	public static String[] defaultPatterns = {
			"(We are)G(the)AA[p|h](\\n)"
	};
	
	public String[] getPatterns(String theType) {
		String[] patterns = null;
		if(theType.startsWith("band")) {
			// generate band names
			List<String> lines = loadFile(bandPatternsFile);
			patterns = new String[lines.size()];
			lines.toArray(patterns);
		}
		else if(theType.startsWith("insults")) {
			// generate insults
			List<String> lines = loadFile(insultPatternsFile);
			patterns = new String[lines.size()];
			lines.toArray(patterns);
		}
		else if(theType.startsWith("fortune")) {
			// invoke the mystic seer
			List<String> lines = loadFile(mysticSeerFile);
			patterns = new String[lines.size()];
			lines.toArray(patterns);
		}
		else if(theType.startsWith("madlib")) {
			// invoke the mystic seer
			List<String> lines = loadFile(madlibFile);
			patterns = new String[lines.size()];
			lines.toArray(patterns);
		}
		else {
			patterns = defaultPatterns;
		}
		return patterns;
	}
	
	public List<String>  loadFile(String filename) {
		List<String> lineList = new ArrayList<>();
        URL url = getClass().getResource(filename);
        if(url == null) {
            throw new IllegalArgumentException("Could not load resource: \"" + filename + "\"");
        }
        try(InputStream stream = url.openStream()) {
        	byte[] bytes = stream.readAllBytes();
        	StringBuilder sb = new StringBuilder();
        	for(byte b : bytes) {
        		sb.append((char)b);
        	}
        	String s = sb.toString();
        	String[] lines = s.split("\r\n");		// Windows
        	for(String l : lines) {
        		if(!l.startsWith("//")) {
        			lineList.add(l);
        		}
        	}
        }
        catch(Exception e) {
        	System.err.println("Could not load " + filename + " " + e.toString());
        }
		return lineList;
	}
	
	public static void main(String...strings) {
		TextDao dao = new TextDao();
		List<String>  patterns = dao.loadFile(TextDao.mysticSeerFile);
		if(patterns != null) {
			for(String l : patterns) {
				System.out.println(l);
			}
		}
	}
}
