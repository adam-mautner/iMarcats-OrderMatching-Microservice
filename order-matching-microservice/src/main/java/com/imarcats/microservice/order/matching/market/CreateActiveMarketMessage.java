package com.imarcats.microservice.order.matching.market;

import com.imarcats.interfaces.client.v100.dto.MarketDto;

public class CreateActiveMarketMessage {

	private MarketDto market;

	public MarketDto getMarket() {
		return market;
	}

	public void setMarket(MarketDto market) {
		this.market = market;
	}
}
