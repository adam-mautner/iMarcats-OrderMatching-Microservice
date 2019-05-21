package com.imarcats.microservice.order.matching.market;

import com.imarcats.interfaces.server.v100.dto.mapping.MarketDtoMapping;
import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;

public class CreateActiveMarketAction {

	private final MarketDatastore marketDatastore;
	
	public CreateActiveMarketAction(MarketDatastore marketDatastoreImpl) {
		super();
		this.marketDatastore = marketDatastoreImpl;
	}

	public void createActiveMarket(CreateActiveMarketMessage message) {
		marketDatastore.createMarket(MarketDtoMapping.INSTANCE.fromDto(message.getMarket()));
	}
	
}
