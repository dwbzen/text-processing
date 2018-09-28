package org.dwbzen.text.util;

import java.io.IOException;

import org.dwbzen.text.util.domain.model.ServiceTicket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Strips off and returns only the 'summary' portion of a JSON trouble ticket.<br>
 * Some examples:<br>
 * input:  {'id': '7818815', 'summary': 'BizDox>Enhancement>Change Company ID'}<br>
 * output: BizDox>Enhancement>Change Company ID<br>
 * input:  {'id': '8056123', 'summary': 'BizDox>"Here" message after selecting \'Clear Needs Work\' for a Credential'}<br>
 * output: BizDox>"Here" message after selecting "Clear Needs Work" for a Credential
 * @author DBacon
 *
 */
public class TicketDataFormatter implements IDataFormatter<String> {

	static ObjectMapper mapper = new ObjectMapper();
	@JsonProperty	private ServiceTicket serviceTicket = null;
	
	public TicketDataFormatter() {

	}
	
	@Override
	public String format(String rawData) {
		String summary = "";
		try {
			rawData = rawData.replace("\"", "\\\"");
			String textData = rawData.replace("'", "\"");
			serviceTicket = mapper.readValue(textData, ServiceTicket.class);
		} catch (IOException e) {
			System.err.println("Exception: " + e.toString());
			e.printStackTrace();
		}
		if(serviceTicket != null && serviceTicket.getSummary().length()>0) {
			summary = serviceTicket.getSummary();
			if(summary.startsWith("'") || summary.startsWith("\"")) {
				// strip the quotes
				summary = summary.substring(1, summary.length());
			}
		}
		return summary;
	}

	public ServiceTicket getServiceTicket() {
		return serviceTicket;
	}
	
	public static void main(String...args) {
		String text = args[0];
		TicketDataFormatter formatter = new TicketDataFormatter();
		String formattedText = formatter.format(text);
		System.out.println(formattedText);
	}

}
