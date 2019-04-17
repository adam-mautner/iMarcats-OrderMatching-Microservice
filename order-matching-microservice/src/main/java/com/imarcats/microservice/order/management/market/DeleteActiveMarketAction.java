package com.imarcats.microservice.order.management.market;

import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;

public class DeleteActiveMarketAction {

	private final MarketDatastore marketDatastore;

	public DeleteActiveMarketAction(MarketDatastore marketDatastore) {
		super();
		this.marketDatastore = marketDatastore;
	}
	
	public void deleteActiveMarket(DeleteActiveMarketMessage message) {
		marketDatastore.deleteMarket(message.getMarketCode()); 
	}
	
}
