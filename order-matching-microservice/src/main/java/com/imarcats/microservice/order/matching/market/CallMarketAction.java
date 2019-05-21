package com.imarcats.microservice.order.matching.market;

import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;
import com.imarcats.internal.server.interfaces.market.MarketInternal;
import com.imarcats.internal.server.interfaces.order.OrderManagementContext;

public class CallMarketAction {
	private final MarketDatastore marketDatastore;
	
	public CallMarketAction(MarketDatastore marketDatastoreImpl) {
		super();
		this.marketDatastore = marketDatastoreImpl;
	}

	public void callMarket(CallMarketMessage message, OrderManagementContext context) {
		MarketInternal marketInternal = marketDatastore.findMarketBy(message.getMarketCode());
		marketInternal.callMarket(context);
	}
}
