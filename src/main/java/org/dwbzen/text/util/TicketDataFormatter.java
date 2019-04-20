package org.dwbzen.text.util;

import java.io.IOException;

import org.dwbzen.text.element.Sentence;
import org.dwbzen.text.element.domain.ServiceTicket;
import org.dwbzen.text.element.domain.ServiceTickets;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Strips off and returns only the 'summary' portion of a single JSON trouble ticket.<br>
 * Some examples:<br>
 * input:  <code><br>
{'id': '9801816', 'summary': 'Workflow Rule> Add new Email Tokens', 'status': 'Dev-Assigned (Alpha)', 
 'team': 'Maint', 'resources': 'JCasuga, MDupre, RAnderson, SRyu', 'board': 'MNG-Alpha', 'age': 301}
</code>
 * output: <code><br>
'TeamCity > GWT builds need to fail for unused imports'<br>
'Cloud Control > New service to run cloud search data import'<br>
</code>
 * NOTE - Can use single or double quotes for field names - "id": or 'id': are both okay/<br>
 * If using single quotes any embedded quotes must be escaped, as in chuck\'s
 * 
 * TODO - deserialize and format ServiceTickets (collection)
 * @author DBacon
 *
 */
public class TicketDataFormatter implements IDataFormatter<String> {

	static ObjectMapper mapper = new ObjectMapper();
	private ServiceTickets serviceTickets = null;
	private ServiceTicket serviceTicket = null;
	
	public TicketDataFormatter() {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}
	
	public Sentence formatAsSentence(String rawData) {
		String formattedData = format(rawData);
		Sentence sentence = new Sentence(formattedData);
		sentence.setId(serviceTicket.getId());
		return sentence;
	}
	
	/**
	 * This makes sure each ServiceTicket summary ends with a sentence delimiter and a newline.
	 */
	@Override
	public String format(String rawData) {
		String summary = "";
		StringBuilder sb = new StringBuilder();
		int start = 0;
		int end = rawData.indexOf('}');
		String rec = null;
		while(end > 0) {
			try {
				rec = rawData.substring(start, end+1);
				serviceTicket = mapper.readValue(rec, ServiceTicket.class);
				summary = serviceTicket.getSummary();
			} catch (IOException e) {
				System.err.println("Exception: " + e.toString() + "\n" + rec);
			}
			if(summary.startsWith("'") || summary.startsWith("\"")) {
				// strip the quotes
				summary = summary.substring(1, summary.length());
			}
			sb.append(summary);
			// check the last character. If it's not a . or ?, add a period to complete the sentence
			char c = summary.charAt(summary.length()-1);
			if(!(c=='?' || c=='.' || c=='!')) {
				sb.append(". ");
			}
			sb.append("\n");
			start = end + 1;
			end = rawData.indexOf('}', start);
		}
		return sb.toString();
	}

	public ServiceTicket getServiceTicket() {
		return serviceTicket;
	}

	public ServiceTickets getServiceTickets() {
		return serviceTickets;
	}
	
	@Override
	public String getKey() {
		return serviceTicket != null ? serviceTicket.getId() : "";
	}
	
	public static void main(String...args) {
		String text = args[0];
		TicketDataFormatter formatter = new TicketDataFormatter();
		String formattedText = formatter.format(text);
		System.out.println(formattedText);
	}

	@Override
	public String getId() {
		return serviceTicket.getId();
	}


}
