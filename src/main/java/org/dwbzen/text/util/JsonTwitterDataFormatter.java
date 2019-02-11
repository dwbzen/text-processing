package org.dwbzen.text.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.dwbzen.text.util.domain.model.TwitterTweet;
import org.dwbzen.text.util.domain.model.TwitterTweets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Strips off and returns the text portion of a JSON tweet
 *  * Example:</p>
 * { "tweets" :
 * [{"source":"Twitter for Android",<br>
 *  "text":"The Mar-a-Lago Club was amazing tonight. Everybody was there, the biggest and the hottest.",<br>
 *  "created_at":"Thu Jan 01 07:02:39 +0000 2015",<br>
 *  "retweet_count":27,<br>
 *  "favorite_count":77,<br>
 *  "is_retweet":false,<br>
 *  "id_str":"550547634218614784"}] }
 * Output: "The Mar-a-Lago Club was amazing tonight. Everybody was there, the biggest and the hottest."
 * @author don_bacon
 *
 */
public class JsonTwitterDataFormatter  implements IDataFormatter<String>  {

	static ObjectMapper mapper = new ObjectMapper();
	@JsonProperty	TwitterTweets twitterTweets = null;
	@JsonProperty	boolean removeUrls = true;	// drop http://... and https://...
	
	public JsonTwitterDataFormatter() { }
	
	
	@Override
	public String format(String rawData) {
		StringBuilder sb = new StringBuilder();
		try {
			twitterTweets = mapper.readValue(rawData, TwitterTweets.class);
		}  catch (IOException e) {
			System.err.println("Exception: " + e.toString());
			e.printStackTrace();
		}
		if(twitterTweets != null) {
			for(TwitterTweet twitterTweet : twitterTweets.getTweets()) {
				String text = twitterTweet.getText();
				if(text != null && text.length()>0) {
					text = cleanText(text);
					// convert embedded returns to space
					sb.append(text.replace('\n', ' ')).append("\n");
				}
			}
		}
		return sb.toString();
	}
	
	public String cleanText(String text) {
		// deal with special characters 
		// convert single quote (Ã¢â‚¬â„¢) to '
		// convert fancy double quote (Ã¢â‚¬Å“) and (Ã¢â‚¬?) to a normal "
		// convert em- (Ã¢â‚¬â€�) and en- (Ã¢â‚¬â€œ) dashes to just a dash
		// convert elipsis (Ã¢â‚¬Â¦) to "..."
		// convert Ã¢â‚¬Ëœ Ã¢â‚¬Å“
		// &amp; to &
		String cleanedText = text.replace("Ã¢â‚¬â„¢", "'");
		text = cleanedText.replace("Ã¢â‚¬Å“", "\"");
		text = text.replace("Ã¢â‚¬ï¿½", "\"");
		text = text.replace("Ã¢â‚¬â€œ", "-");
		text = text.replace("Ã¢â‚¬â€�", "-");
		text = text.replace("Ã¢â‚¬Â¦", "...");
		text = text.replace("&amp;", "&");
		if(removeUrls) {
			text = removeString(text, "https:");
			text = removeString(text, "http:");
		}
		return text;
	}
	
	public static String removeString(String text, String textToRemove) {
		int ind = -1;
		while((ind = text.indexOf(textToRemove)) >= 0) {
			int ind2 = text.indexOf(" ", ind);
			if(ind == 0) {
				text = (ind2 < 0) ? "" : text.substring(ind2);
			}
			else {
				text = (ind2 < 0) ? text.substring(0, ind-1) : text.substring(0, ind-1) + text.substring(ind2);
			}
		}
		return text.trim();
	}

	public static void main(String...args) throws FileNotFoundException {
		String inputFile = args[0];
		DataSourceDescription dataSourceDescription = new DataSourceDescription(DataSourceType.TextFile);
		dataSourceDescription.getProperties().setProperty("filename", inputFile);
		dataSourceDescription.getProperties().setProperty("eol", "\n");
		TextFileDataSource dataSource = new TextFileDataSource(dataSourceDescription);
		String sourceText = dataSource.getData();
		
		JsonTwitterDataFormatter formatter = new JsonTwitterDataFormatter();
		String formattedText = formatter.format(sourceText);
		System.out.println(formattedText);
	}
}
