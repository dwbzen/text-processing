package org.dwbzen.text.junit;

import java.util.List;

import org.dwbzen.text.util.model.TextGenerator;
import org.junit.Test;

import junit.framework.TestCase;

public class TextGeneratorTest extends TestCase  {

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
	
	@Test
	public void testGenerateText() {
		TextGenerator generator = TextGenerator.newInstance();
		generator.setPatternList(bandPatterns);
		generator.setPostProcessing("TC");
		generator.generate(20);
		List<String> generatedText = generator.getGeneratedText();
		for(String name : generatedText) {
			System.out.println(name);
		}
	}
}