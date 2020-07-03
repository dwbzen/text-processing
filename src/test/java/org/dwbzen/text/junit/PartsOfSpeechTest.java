package org.dwbzen.text.junit;
import org.dwbzen.common.util.IJson;
import org.dwbzen.text.pos.PartOfSpeech;
import org.dwbzen.text.pos.PartsOfSpeech;
import org.junit.Test;

import junit.framework.TestCase;

public class PartsOfSpeechTest  extends TestCase {

	@Test
	public void testPartsOfSpeech() {

		PartOfSpeech pos = PartsOfSpeech.PartsOfSpeechDefinitionMap.get("F");
		
		System.out.println("\nCheck deserialization");
		String posJson = pos.toJson();
		System.out.println(posJson);
		PartOfSpeech pos2 = null;
		try {
			pos2 = IJson.mapper.readValue(posJson, PartOfSpeech.class);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		System.out.println(pos2.toJson());
		assertEquals(pos, pos2);
	}
}
