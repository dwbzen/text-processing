package org.dwbzen.text.util;

import java.io.IOException;

import org.dwbzen.text.util.domain.model.ServiceTicket;
import org.dwbzen.text.util.domain.model.ServiceTickets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Strips off and returns only the 'summary' portion of a JSON trouble ticket.<br>
 * Some examples:<br>
 * input:  <code><br>
{ "serviceTickets" : [<br>
{'id': '3392541', 'summary': 'TeamCity > GWT builds need to fail for unused imports'},<br>
{'id': '7225692', 'summary': 'Cloud Control > New service to run cloud search data import'} }]<br>
</code>
 * output: <code><br>
'TeamCity > GWT builds need to fail for unused imports'<br>
'Cloud Control > New service to run cloud search data import'<br>
</code>
 * NOTE - Can use single or double quotes for field names - "id": or 'id': are both okay/<br>
 * If using single quotes any embedded quotes must be escaped, as in chuck\'s
 *
 * @author DBacon
 *
 */
public class TicketDataFormatter implements IDataFormatter<String> {

	static ObjectMapper mapper = new ObjectMapper();
	@JsonProperty	private ServiceTickets serviceTickets = null;
	
	public TicketDataFormatter() {

	}
	
	@Override
	public String format(String rawData) {
		String summary = "";
		StringBuilder sb = new StringBuilder();
		try {
			rawData = rawData.replace("\"", "\\\"");
			String textData = rawData.replace("'", "\"");
			serviceTickets = mapper.readValue(textData, ServiceTickets.class);
		} catch (IOException e) {
			System.err.println("Exception: " + e.toString());
			e.printStackTrace();
		}
		if(serviceTickets != null && serviceTickets.size() > 0) {
			for(ServiceTicket serviceTicket : serviceTickets.getServiceTickets()) {
				summary = serviceTicket.getSummary();
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
			}
		}
		return sb.toString();
	}

	public ServiceTickets getServiceTickets() {
		return serviceTickets;
	}
	
	public static void main(String...args) {
		String text = args[0];
		TicketDataFormatter formatter = new TicketDataFormatter();
		String formattedText = formatter.format(text);
		System.out.println(formattedText);
	}

}
