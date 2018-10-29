package org.dwbzen.text.util;

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
					// convert embedded returns to space
					// deal with special characters 
					// convert single quote (’) to '
					// convert fancy double quote (“) and (�?) to a normal "
					// convert em- and en- (–) dashes to just a dash
					// convert elipsis (…) to "..."
					// concert ‘ 
					String cleanedText = text.replaceAll("’", "'");
					text = cleanedText.replaceAll("“", "\"");
					text = text.replace("�?", "\"");
					text = text.replaceAll("–", "-");
					text = text.replaceAll("…", "...");
					sb.append(text.replace('\n', ' ')).append("\n");
				}
			}
		}
		return sb.toString();
	}

	public static void main(String...args) {
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
