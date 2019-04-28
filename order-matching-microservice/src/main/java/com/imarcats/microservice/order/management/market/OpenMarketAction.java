package com.imarcats.microservice.order.management.market;

import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;
import com.imarcats.internal.server.interfaces.market.MarketInternal;
import com.imarcats.internal.server.interfaces.order.OrderManagementContext;

public class OpenMarketAction {
	private final MarketDatastore marketDatastore;
	
	public OpenMarketAction(MarketDatastore marketDatastoreImpl) {
		super();
		this.marketDatastore = marketDatastoreImpl;
	}

	public void openMarket(OpenMarketMessage message, OrderManagementContext context) {
		MarketInternal marketInternal = marketDatastore.findMarketBy(message.getMarketCode());
		marketInternal.openMarket(context); 
	}
}
