package org.dwbzen.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.dwbzen.text.pos.TextGenerator;

@Path("/TextService") 
public class TextService {

	private TextDao textDao = new TextDao();
	
	@GET 
	@Path("/{typeid}/{number}") 
	@Produces(MediaType.APPLICATION_JSON) 
	public List<String> generateText(@PathParam("typeid") String typeid, @PathParam("number") int num) {
		// typeid = bands | drugs | insult | fortune | madlib
		List<String> generatedText = null;
		String theType = typeid.toLowerCase();
		if(theType.startsWith("band")) {
			// http://localhost:8080/text-service/rest/TextService/bands/20
			generatedText = generateFromPatternFile("bands", "TC", num);
		}
		else if(theType.startsWith("drug")) {
			// http://localhost:8080/text-service/rest/TextService/drugs/20
			generatedText = generateDrugNames(num);
		}
		else if(theType.startsWith("insult")) {
			// http://localhost:8080/text-service/rest/TextService/insults/20
			generatedText = generateFromPatternFile("insults", "NC", num);
		}
		else if(theType.startsWith("fortune")) {
			// http://localhost:8080/text-service/rest/TextService/fortune/1
			generatedText = generateFromPatternFile("fortune", "NC", 1);
		}
		else if(theType.startsWith("madlib")) {
			// http://localhost:8080/text-service/rest/TextService/madlib/2
			List<String> temp = generateFromPatternFile("madlib", "NC", num);
			generatedText = breakUpLines(temp, "\n");
		}
		else if(theType.startsWith("poem")) {
			// http://localhost:8080/text-service/rest/TextService/poem/1
			List<String> temp = generateFromPatternFile("poem", "SC", num);
			generatedText = breakUpLines(temp, "\n");
		}
		return generatedText;
	}
	
	private List<String> breakUpLines(List<String> temp, String delim) {
		List<String> generatedText = new ArrayList<>();
		/*
		 * Break up each string into lines delimited by "\n"
		 */
		for(String text : temp) {
			String[] lines = text.split(delim);
			for(String s : lines) {
				generatedText.add(s);
			}
			// add a blank line
			generatedText.add("");
		}
		return generatedText;
	}
	
	@POST
	@Path("/pattern/{number}")
	@Consumes(MediaType.TEXT_PLAIN)
	public List<String> generateText(@PathParam("number") int num, String message) {
		List<String> generatedText = new ArrayList<>();
		TextGenerator generator = TextGenerator.newInstance();
		if(generator == null) {
			generatedText.add("There was a problem creating TextGenerator");
		}
		else {
			List<String> patternList = new ArrayList<>();
			patternList.add(message);
			generator.setPatternList(patternList);
			generatedText.addAll(generator.generateText(num));
		}
		return generatedText;
	}
	
	@GET 
	@Path("/bands") 
	@Produces(MediaType.APPLICATION_JSON) 
	public List<String> generateText(@PathParam("typeid") String typeid) {
		return generateFromPatternFile("bands", "TC", 50);
	}

	public List<String> generateDrugNames(int num) {
		List<String> generatedText = new ArrayList<>();
		
		generatedText.add("ingenzomin");
		
		return generatedText;
	}
	
	public List<String> generateFromPatternFile(String type, String postProcessing, int num) {
		TextGenerator generator = new TextGenerator();
		List<String> generatedText = new ArrayList<>();
		String[] patterns = textDao.getPatterns(type);
		generator.setPatternList(patterns);
		generator.setPostProcessing(postProcessing);
		generator.apply(num);
		generatedText.addAll(generator.getGeneratedText());
		return generatedText;
	}

	public static void main(String...strings ) {
		TextService service = new TextService();
		String gentype = strings != null ? strings[0] : "fortune";
		List<String> textList = service.generateText(gentype, 1);
		for(String text : textList) {
			System.out.println(text);
		}
	}
}
