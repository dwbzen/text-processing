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
			// http://localhost:8080/text-service/rest/TextService/madlib/10
			generatedText = generateFromPatternFile("madlib", "NC", num);
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

	private List<String> generateDrugNames(int num) {
		List<String> generatedText = new ArrayList<>();
		
		generatedText.add("ingenzomin");
		
		return generatedText;
	}
	
	private List<String> generateFromPatternFile(String type, String postProcessing, int num) {
		TextGenerator generator = TextGenerator.newInstance();
		List<String> generatedText = new ArrayList<>();
		if(generator == null) {
			generatedText.add("There was a problem creating TextGenerator");
		}
		else {
			String[] patterns = textDao.getPatterns(type);
			generator.setPatternList(patterns);
			generator.setPostProcessing(postProcessing);
			generator.generate(num);
			generatedText.addAll(generator.getGeneratedText());
		}
		return generatedText;
	}


}
