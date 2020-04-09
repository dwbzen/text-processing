package org.dwbzen.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.dwbzen.common.cp.MarkovChain;
import org.dwbzen.text.cp.CharacterCollector;
import org.dwbzen.text.cp.WordProducer;
import org.dwbzen.text.element.Sentence;
import org.dwbzen.text.element.Word;
import org.dwbzen.text.pos.TextGenerator;

@Path("/TextService") 
public class TextService {

	private TextDao textDao = new TextDao();
	private CharacterCollector collector = null;
	private WordProducer producer = null;
	
	@GET 
	@Path("/{typeid}/{number}") 
	@Produces(MediaType.APPLICATION_JSON) 
	public List<String> generateText(@PathParam("typeid") String typeid, @PathParam("number") int num,  
							@DefaultValue("2") @QueryParam("order") int order,
							@DefaultValue("female") @QueryParam("gender") String queryParams) {
		//
		// typeid = bands | drugNames?order=n | firstNames?order=n | insult | fortune | madlib
		//
		List<String> generatedText = null;
		String theType = typeid.toLowerCase();
		if(theType.startsWith("band")) {
			// http://localhost:8010/text-service/rest/TextService/bands/20
			generatedText = generateFromPatternFile("bands", "TC,JSON", num);
		}
		else if(theType.equalsIgnoreCase("drugNames")) {
			// sample: http://localhost:8010/text-service/rest/TextService/drugNames/20?order=3
			// -file  "$RESOURCE/drugNames.txt"  -order 2 -list -ignoreCase -num 20 -min 5 -format "TC" -init -stat false
			generatedText = generateDrugNames(num, order, queryParams);
		}
		else if(theType.equalsIgnoreCase("firstNames")) {
			// sample: http://localhost:8010/text-service/rest/TextService/firstNames/10?gender=male&order=3
			// -file  "$RESOURCE/drugNames.txt"  -order 2 -list -ignoreCase -num 20 -min 5 -format "TC" -init -stat false
			generatedText = generateFirstNames(num, order, queryParams);
		}
		else if(theType.startsWith("insult")) {
			// http://localhost:8010/text-service/rest/TextService/insults/20
			generatedText = generateFromPatternFile("insults", "NC", num);
		}
		else if(theType.startsWith("fortune")) {
			// http://localhost:8010/text-service/rest/TextService/fortune/1
			generatedText = generateFromPatternFile("fortune", "NC", 1);
		}
		else if(theType.startsWith("madlib")) {
			// http://localhost:8080/text-service/rest/TextService/madlib/2
			List<String> temp = generateFromPatternFile("madlib", "NC", num);
			generatedText = breakUpLines(temp, "\\R");		// regex end of line all platforms
		}
		else if(theType.startsWith("poem")) {
			// http://localhost:8010/text-service/rest/TextService/poem/1
			List<String> temp = generateFromPatternFile("poem", "SC", num);
			generatedText = breakUpLines(temp, "\\R");
		}
		return generatedText;
	}
	
	private List<String> breakUpLines(List<String> temp, String delim) {
		List<String> generatedText = new ArrayList<>();
		/*
		 * Break up each string into lines delimited by delim character
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
		TextGenerator generator = TextGenerator.newInstance(Collections.emptyList());
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

	public List<String> generateFirstNames(int num, int order, String queryParams) {
		return generateFromWordProducer("firstNames", "TC", order, num, queryParams);
	}
	
	public List<String> generateDrugNames(int num, int order, String queryParams) {
		return generateFromWordProducer("drugNames", "TC", order, num, queryParams);
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
	
	public List<String> generateFromWordProducer(String type, String postProcessing, int order, int num, String queryParams) {
		List<String> generatedText = new ArrayList<>();
		List<String> fileLines = textDao.loadResource(type, queryParams);
		boolean ignoreCase = true;
		boolean pickInitialSeed = true;
		// Step 1 - run the CharacterCollector
		collector = new CharacterCollector(order, fileLines, ignoreCase);
		collector.setTrace(false);
		collector.collect();
		MarkovChain<Character, Word, Sentence> markovChain = collector.getMarkovChain();
		
		// Step 2 - run WordProducer using the MarkovChain result from the collector
		markovChain.setPickInitialSeed(pickInitialSeed);
		Word seed = markovChain.pickSeed();
		producer = WordProducer.instance(order, markovChain, seed);
		producer.setNumberOfWords(num);
		producer.setMinimumWordLength(5);
		producer.setMaximumLength(10);
		producer.setSortedResult(true);
		producer.setStatisticalPick(true);
		Set<Word> words = producer.produce(false);
		words.forEach(w -> {
			String s = formatWord(w, postProcessing);
			generatedText.add(s);
		});
		
		return generatedText;
	}
	
	public static String formatWord(Word word, String postProcessing) {
		String wordString = null;
		if(postProcessing.equalsIgnoreCase("NC")) {
			wordString = word.toString().trim();
		}
		else if(postProcessing.equalsIgnoreCase("UC")) {
			wordString = word.toString().toUpperCase();
		}
		else if(postProcessing.equalsIgnoreCase("LC")) {
			wordString = word.toString().toLowerCase();
		}
		else if(postProcessing.equalsIgnoreCase("TC")) {
			wordString = word.toString().toLowerCase().trim();
			StringBuilder sb = new StringBuilder(wordString.substring(0, 1).toUpperCase());
			sb.append(wordString.substring(1));
			wordString = sb.toString();
		}
		else {
			wordString = word.toString();
		}
		return wordString;
	}

	public static void main(String...strings ) {
		TextService service = new TextService();
		String gentype = strings != null && strings.length>0 ? strings[0] : "fortune";
		int num = strings != null && strings.length == 2 ? Integer.parseInt(strings[1]) : 1;
		List<String> textList = service.generateText(gentype, num, 3, "male");
		for(String text : textList) {
			System.out.println(text);
		}
	}
}
