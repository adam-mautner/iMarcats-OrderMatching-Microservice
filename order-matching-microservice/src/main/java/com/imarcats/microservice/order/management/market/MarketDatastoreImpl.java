package com.imarcats.microservice.order.management.market;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;
import com.imarcats.internal.server.infrastructure.datastore.MatchedTradeDatastore;
import com.imarcats.internal.server.infrastructure.datastore.OrderDatastore;
import com.imarcats.internal.server.interfaces.market.MarketInternal;
import com.imarcats.market.engine.market.MarketImpl;
import com.imarcats.model.Market;
import com.imarcats.model.types.ActivationStatus;
import com.imarcats.model.types.PagedMarketList;

@Component("MarketDatastoreImpl")
public class MarketDatastoreImpl implements MarketDatastore {


	private HashMap<String, Market> markets = new HashMap<String, Market>();
	
	@Autowired
	@Qualifier("MatchedTradeDatastoreImpl")
	protected MatchedTradeDatastore tradeDatastore;

	// this will be set manually to avoid circular dependencies 
	private OrderDatastore orderDatastore;
	
	@Override
	public String createMarket(Market market) {
		return markets.put(market.getMarketCode(), market).getMarketCode();
	}

	@Override
	public void deleteMarket(String code) {
		markets.remove(code);
	}

	@Override
	public MarketInternal findMarketBy(String code) {
		Market market = markets.get(code);
		// TODO: Fix this, we should replace the datastores with implementation that call web service instead of database access
		// 		 the best would be to have access to these datastores here at all 
		return market != null ? new MarketImpl(market, null, orderDatastore, this, null, tradeDatastore): null; 
	}
	
	@Override
	public Market updateMarket(Market changedMarket) {
		return markets.put(changedMarket.getMarketCode(), changedMarket);
	}

	@Override
	public PagedMarketList findAllMarketModelsFromCursor(String cursorString, int numberOnPage) {
		return createPagedMarketList(markets.values().stream().collect(Collectors.toList()), cursorString, numberOnPage);
	}

	@Override
	public Market[] findMarketModelsByBusinessEntity(String businessEntity) {
		return markets.values().stream().filter(market -> market.getBusinessEntityCode().equals(businessEntity)).toArray(size -> new Market[size]);
	}

	@Override
	public Market[] findMarketModelsByInstrument(String instrument) {
		return findMarketModelsByInstrumentStream(instrument).toArray(size -> new Market[size]);
	}

	private Stream<Market> findMarketModelsByInstrumentStream(String instrument) {
		return markets.values().stream().filter(market -> market.getInstrumentCode().equals(instrument));
	}

	@Override
	public Market[] findMarketModelsByMarketOperator(String marketOperator) {
		return findMarketModelsByMarketOperatorStream(marketOperator).toArray(size -> new Market[size]);
	}

	private Stream<Market> findMarketModelsByMarketOperatorStream(String marketOperator) {
		return markets.values().stream().filter(market -> market.getMarketOperatorCode().equals(marketOperator));
	}

	@Override
	public PagedMarketList findMarketModelsFromCursorByActivationStatus(ActivationStatus activationStatus, String cursorString, int numberOnPage) {
		return createPagedMarketList(markets.values().stream().filter(markets -> markets.getActivationStatus() == activationStatus).collect(Collectors.toList()), cursorString, numberOnPage);
	}

	@Override
	public PagedMarketList findMarketModelsFromCursorByInstrument(String instrument, String cursorString, int numberOnPage) {
		return createPagedMarketList(findMarketModelsByInstrumentStream(instrument).collect(Collectors.toList()), cursorString, numberOnPage);
	}

	@Override
	public PagedMarketList findMarketModelsFromCursorByMarketOperator(String marketOperator, String cursorString, int numberOnPage) {
		return createPagedMarketList(findMarketModelsByMarketOperatorStream(marketOperator).collect(Collectors.toList()), cursorString, numberOnPage);
	}

	private PagedMarketList createPagedMarketList(List<Market> markets, String cursorString_, int maxNumberOfMatchedTradeSidesOnPage_) {
		int start = Integer.parseInt(cursorString_); 
		int end = (start + maxNumberOfMatchedTradeSidesOnPage_) > markets.size() ? markets.size() : (start + maxNumberOfMatchedTradeSidesOnPage_);
		
		List<Market> sublist = markets.subList(start, end);
		
		PagedMarketList list = new PagedMarketList();
		list.setMarkets(sublist.toArray(new Market[sublist.size()]));
		list.setCursorString(""+(end + 1));
		list.setMaxNumberOfMarketsOnPage(sublist.size());
		 
		return list;
	}
	
	public OrderDatastore getOrderDatastore() {
		return orderDatastore;
	}

	public void setOrderDatastore(OrderDatastore orderDatastore) {
		this.orderDatastore = orderDatastore;
	}
}
