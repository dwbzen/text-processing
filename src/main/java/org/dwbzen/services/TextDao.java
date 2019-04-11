package org.dwbzen.services;

public class TextDao {

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
	
	public static String[] getPatterns(String theType) {
		String[] patterns = null;
		if(theType.startsWith("band")) {
			// generate band names
			patterns = bandPatterns;
		}
		else if(theType.startsWith("insults")) {
			// generate insults
			patterns = insultPatterns;
		}
		else {
			patterns = defaultPatterns;
		}
		return patterns;
	}
	
}
