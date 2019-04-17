package com.imarcats.microservice.order.management.market;

import com.imarcats.microservice.order.management.UpdateMessage;

public class DeleteActiveMarketMessage extends UpdateMessage {

	private String marketCode;

	public String getMarketCode() {
		return marketCode;
	}

	public void setMarketCode(String marketCode) {
		this.marketCode = marketCode;
	}
	
}
