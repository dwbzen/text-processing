package org.dwbzen.text.element.domain;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Example:</p>
 * {"source":"Twitter for Android",<br>
 *  "text":"The Mar-a-Lago Club was amazing tonight. Everybody was there, the biggest and the hottest.",<br>
 *  "created_at":"Thu Jan 01 07:02:39 +0000 2015",<br>
 *  "retweet_count":27,<br>
 *  "favorite_count":77,<br>
 *  "is_retweet":false,<br>
 *  "id_str":"550547634218614784"}
 *
 * @author don_bacon
 *
 */
public class TwitterTweet implements IJson {

	@JsonProperty("source")			private String source;
	@JsonProperty("text")			private String text;
	@JsonProperty("created_at")		private String created_at;
	@JsonProperty("retweet_count")	private int retweet_count;
	@JsonProperty("favorite_count")	private int favorite_count;
	@JsonProperty("is_retweet")		private boolean is_retweet;
	@JsonProperty("id_str")			private String id_str;
	
	public TwitterTweet() { }
	
	public TwitterTweet(String source, String text, String created, int retweets, int favorites, boolean retweet, String id) {
		this.source = source;
		this.text = text;
		this.created_at = created;
		this.retweet_count = retweets;
		this.favorite_count = favorites;
		this.is_retweet = retweet;
		this.id_str = id;
	}

	public String getSource() {
		return source;
	}

	public TwitterTweet setSource(String source) {
		this.source = source;
		return this;
	}

	public String getText() {
		return text;
	}

	public TwitterTweet setText(String text) {
		this.text = text;
		return this;
	}

	public String getCreated_at() {
		return created_at;
	}

	public TwitterTweet setCreated_at(String created_at) {
		this.created_at = created_at;
		return this;
	}

	public int getRetweet_count() {
		return retweet_count;
	}

	public TwitterTweet setRetweet_count(int retweet_count) {
		this.retweet_count = retweet_count;
		return this;
	}

	public int getFavorite_count() {
		return favorite_count;
	}

	public TwitterTweet setFavorite_count(int favorite_count) {
		this.favorite_count = favorite_count;
		return this;
	}

	public boolean isIs_retweet() {
		return is_retweet;
	}

	public TwitterTweet setIs_retweet(boolean is_retweet) {
		this.is_retweet = is_retweet;
		return this;
	}

	public String getId_str() {
		return id_str;
	}

	public TwitterTweet setId_str(String id_str) {
		this.id_str = id_str;
		return this;
	}

}
