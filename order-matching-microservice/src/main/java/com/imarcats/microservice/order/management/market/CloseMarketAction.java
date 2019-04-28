package com.imarcats.microservice.order.management.market;

import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;
import com.imarcats.internal.server.interfaces.market.MarketInternal;
import com.imarcats.internal.server.interfaces.order.OrderManagementContext;

public class CloseMarketAction {
	private final MarketDatastore marketDatastore;
	
	public CloseMarketAction(MarketDatastore marketDatastoreImpl) {
		super();
		this.marketDatastore = marketDatastoreImpl;
	}

	public void closeMarket(CloseMarketMessage message, OrderManagementContext context) {
		MarketInternal marketInternal = marketDatastore.findMarketBy(message.getMarketCode());
		marketInternal.closeMarket(context); 
	}
}
