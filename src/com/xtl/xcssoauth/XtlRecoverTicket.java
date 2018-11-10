package com.xtl.xcssoauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.xtl.xcssoauth.pojo.XtlTicket;

public class XtlRecoverTicket implements Runnable {
	
	private Map<String, XtlTicket> tickets;
	
	public XtlRecoverTicket(Map<String, XtlTicket> tickets) {
		super();
		this.tickets = tickets;
	}

	@Override
	public void run() {
		List<String> ticketKeys = new ArrayList<String>();
		for(Entry<String, XtlTicket> entry : tickets.entrySet()) {
			if(entry.getValue().getRecoverTime().getTime() < System.currentTimeMillis())
				ticketKeys.add(entry.getKey());
		}
		for(String ticketKey : ticketKeys) {
			tickets.remove(ticketKey);
			System.out.println("ticket[" + ticketKey + "]过期已删除！");
		}
	}

}
