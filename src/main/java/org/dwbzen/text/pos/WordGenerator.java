package org.dwbzen.text.pos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

/**
 * Generates word(s) from a character pattern
 * Usage: WordGenerator [options] [pattern1 pattern2...]
 * Where: 
 * 	pattern1, pattern2...  :  patterns to use for word generation. 
 * Options:
 *  -n <number>		: number of strings to generate
 *  -format			: output format(s) processing:
 *  					UC = convert to UPPER CASE, 
 *						LC = lower case,
 *						TC = Title Case, 
 *						NC = no conversion (default)
 *						CSV = comma-separated words
 *						TAB = tab-separated words
 *						JSON:fieldname = as JSON array
 * 
 * Patterns use the following character type:
 * 
 * @author DBacon
 *
 */
public class WordGenerator implements Supplier<String> {
	protected static final Logger log = LogManager.getLogger(TextGenerator.class);
	
	@Override
	public String get() {
		// TODO Auto-generated method stub
		return null;
	}

}
