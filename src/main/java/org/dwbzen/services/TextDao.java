package org.dwbzen.services;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TextDao {

	public TextDao() {
		
	}
	
	public static String mysticSeerFile = "/reference/pos/mysticSeer.txt";
	public static String madlibFile = "/reference/pos/madlib.txt";
	public static String bandPatternsFile = "/reference/pos/bandNamePatterns.txt";
	public static String insultPatternsFile = "/reference/pos/insultPatterns.txt";
	public static String drugNamesFile = "/reference/drugNames.txt";
	public static String poetryPatternsFile = "/reference/pos/poetryPatterns.txt";
	public static String maleNamesFile = "/reference/maleFirstNames.txt";
	public static String femaleNamesFile = "/reference/femaleFirstNames.txt";

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
			// fill in a mad lib
			List<String> lines = loadFile(madlibFile);
			patterns = new String[lines.size()];
			lines.toArray(patterns);
		}
		else if(theType.startsWith("poem")) {
			//  create beat poem
			List<String> lines = loadFile(poetryPatternsFile);
			patterns = new String[lines.size()];
			lines.toArray(patterns);
		}
		else {
			patterns = defaultPatterns;
		}
		return patterns;
	}
	
	public List<String>  loadResource(String type, String params) {
		List<String> lines = new ArrayList<>();
		if(type.equalsIgnoreCase("drugNames")) {
			lines = loadFile(drugNamesFile);
		}
		else if(type.equalsIgnoreCase("firstNames")) {
			lines = params.equalsIgnoreCase("male") ? loadFile(maleNamesFile) : loadFile(femaleNamesFile);
		}
		return lines;
	}
	
	/**
	 * Loads a File as a resource
	 * @param filename a resource text file
	 * @return List<String> pattern lines
	 */
	public List<String>  loadFile(String filename) {
		List<String> patterns = new ArrayList<>();
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
        	String[] lines = s.split("\\R");		// Unicode linebreak sequence
			sb = new StringBuilder();
        	for(String line : lines) {
				if(line.startsWith("//"))
					continue;
				if(line.endsWith("+")) {
					sb.append(line.substring(0, line.length() - 1));
				}
				else if(sb.length() > 0 ) {
					sb.append(line);
					patterns.add(sb.toString());
					sb = new StringBuilder();
				}
				else
					patterns.add(line);
        	}
        }
        catch(Exception e) {
        	System.err.println("Could not load " + filename + " " + e.toString());
        }
		return patterns;
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
