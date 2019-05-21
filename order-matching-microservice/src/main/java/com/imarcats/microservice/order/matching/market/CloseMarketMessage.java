package com.imarcats.microservice.order.matching.market;

public class CloseMarketMessage {
	private String marketCode;

	public String getMarketCode() {
		return marketCode;
	}

	public void setMarketCode(String marketCode) {
		this.marketCode = marketCode;
	}
}
