package org.dwbzen.text.element.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.IJson;

public class TwitterTweets implements IJson  {

	private static final long serialVersionUID = -4788032781083735344L;
	
	@JsonProperty	List<TwitterTweet>	tweets = new ArrayList<>();
	
	public TwitterTweets() {
		
	}

	public List<TwitterTweet> getTweets() {
		return tweets;
	}
	
	public TwitterTweets addTweet(TwitterTweet tweet) {
		tweets.add(tweet);
		return this;
	}
	
	public int size() {
		return tweets.size();
	}
	
	public static void main(String... args) {
		TwitterTweets tweets = new TwitterTweets();
		TwitterTweet tweet1 = new TwitterTweet("Twitter", "What a joke!", "2018-10-19", 2, 2, false, "550547634218614784");
		TwitterTweet tweet2 = new TwitterTweet("Twitter", "It is a hoax!", "2018-10-19", 3, 3, false, "990547634218614784");
		tweets.addTweet(tweet1).addTweet(tweet2);
		String json = tweets.toJson(true);
		System.out.println(json);
	}

}
