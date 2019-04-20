package org.dwbzen.text.element.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.IJson;

public class ServiceTickets implements IJson {

	private static final long serialVersionUID = -5295059730543973202L;
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
