package org.dwbzen.text.element.domain;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceTickets implements IJson {

	@JsonProperty	private List<ServiceTicket> serviceTickets  = new ArrayList<>();
	
	public ServiceTickets() {
		
	}
	
	public List<ServiceTicket> getServiceTickets() {
		return serviceTickets;
	}
	
	public ServiceTickets addServiceTicket(ServiceTicket ticket) {
		serviceTickets.add(ticket);
		return this;
	}
	
	public int size() {
		return serviceTickets.size();
	}
}
