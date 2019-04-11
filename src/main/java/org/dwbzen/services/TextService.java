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

	@GET 
	@Path("/{typeid}/{number}") 
	@Produces(MediaType.APPLICATION_JSON) 
	public List<String> generateText(@PathParam("typeid") String typeid, @PathParam("number") int num) {
		// typeid = bands | drugs | insults
		List<String> generatedText = null;
		String theType = typeid.toLowerCase();
		if(theType.startsWith("band")) {
			// http://localhost:8080/text-service/rest/TextService/bands/20
			generatedText = generateBandNames(num);
		}
		else if(theType.startsWith("drug")) {
			// http://localhost:8080/text-service/rest/TextService/drugs/20
			generatedText = generateDrugNames(num);
		}
		else if(theType.startsWith("insult")) {
			// http://localhost:8080/text-service/rest/TextService/insults/20
			generatedText = generateInsults(num);
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
		return generateBandNames(50);
	}

	private List<String> generateInsults(int num) {
		List<String> generatedText = new ArrayList<>();
		TextGenerator generator = TextGenerator.newInstance();
		if(generator == null) {
			generatedText.add("There was a problem creating TextGenerator");
		}
		else {
			String[] patterns = TextDao.getPatterns("insult");
			generator.setPatternList(patterns);
			generatedText.addAll(generator.generateText(num));
		}
		return generatedText;
	}

	private List<String> generateDrugNames(int num) {
		List<String> generatedText = new ArrayList<>();
		
		generatedText.add("ingenzomin");
		
		return generatedText;
	}

	/*
	 * -lib  "build/resources/main/reference/bandNamePatterns.txt" -n num -format TC
	 * @param num
	 * @return
	 */
	private List<String> generateBandNames(int num) {
		TextGenerator generator = TextGenerator.newInstance();
		List<String> generatedText = new ArrayList<>();
		if(generator == null) {
			generatedText.add("There was a problem creating TextGenerator");
		}
		else {
			String[] patterns = TextDao.getPatterns("band");
			generator.setPatternList(patterns);
			generator.setPostProcessing("TC");
			generator.generate(num);
			generatedText.addAll(generator.getGeneratedText());
		}
		return generatedText;
	}
}
