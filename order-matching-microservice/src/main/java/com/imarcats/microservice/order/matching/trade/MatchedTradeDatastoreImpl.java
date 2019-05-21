package com.imarcats.microservice.order.matching.trade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.imarcats.internal.server.infrastructure.datastore.MatchedTradeDatastore;
import com.imarcats.model.MatchedTrade;
import com.imarcats.model.TradeSide;
import com.imarcats.model.types.PagedMatchedTradeSideList;

@Component("MatchedTradeDatastoreImpl")
public class MatchedTradeDatastoreImpl implements MatchedTradeDatastore {

	private HashMap<Long, MatchedTrade> trades = new HashMap<Long, MatchedTrade>();
	private AtomicLong transactionId = new AtomicLong();
	
	@Override
	public Long createMatchedTrade(MatchedTrade matchedTrade_) {
		checkReferenceUniqueness(matchedTrade_.getBuySide());
		checkReferenceUniqueness(matchedTrade_.getSellSide());
		long transactionIdValue = transactionId.incrementAndGet();
		matchedTrade_.setTransactionID(transactionIdValue);
		return trades.put(transactionIdValue, matchedTrade_).getTransactionID();
	}

	private void checkReferenceUniqueness(TradeSide tradeSide) {
		if(findMatchedTradeByReferenceUserAndMarket(tradeSide.getExternalOrderReference(), tradeSide.getTraderID(), tradeSide.getMarketOfTheTrade()) != null) {
			throw new RuntimeException("Non-Unique external reference: " + tradeSide);
		}
	}

	@Override
	public MatchedTrade[] findAllMatchedTradeByMarket(String marketCode_) {
		return trades.values().stream().filter(trade -> trade.getMarketOfTheTrade().equals(marketCode_)).toArray(size -> new MatchedTrade[size]);
	}

	@Override
	public TradeSide findMatchedTradeByReferenceUserAndMarket(String externalReference_, String userID_, String marketCode_) {
		return Arrays.asList(findMatchedTradeByUserAndMarketInternal(userID_, marketCode_)).stream().filter(trade -> trade.getExternalOrderReference().equals(externalReference_)).findFirst().get();
	}

	@Override
	public MatchedTrade findMatchedTradeByTransactionId(Long transactionId_) {
		return trades.get(transactionId_);  
	}

	@Override
	public PagedMatchedTradeSideList findMatchedTradeByUser(String userID_, String cursorString_, int maxNumberOfMatchedTradeSidesOnPage_) {
		return createPagedTradeSideList(Arrays.asList(findMatchedTradeByUserInternal(userID_)), cursorString_, maxNumberOfMatchedTradeSidesOnPage_);
	}

	@Override
	public PagedMatchedTradeSideList findMatchedTradeByUserAndMarket(String userID_, String marketCode_, String cursorString_, int maxNumberOfMatchedTradeSidesOnPage_) {
		return createPagedTradeSideList(Arrays.asList(findMatchedTradeByUserAndMarketInternal(userID_, marketCode_)), cursorString_, maxNumberOfMatchedTradeSidesOnPage_);
	}

	@Override
	public TradeSide[] findMatchedTradeByUserAndMarketInternal(String userID_, String marketCode_) {
		List<MatchedTrade> tradeList = Arrays.asList(findAllMatchedTradeByMarket(marketCode_));
		return turnTradeToTradeSide(tradeList);
	}

	private TradeSide[] turnTradeToTradeSide(Collection<MatchedTrade> tradeList) {
		List<TradeSide> buySideList = tradeList.stream().map(trade -> trade.getBuySide()).collect(Collectors.toList());
		List<TradeSide> sellSideList = tradeList.stream().map(trade -> trade.getSellSide()).collect(Collectors.toList());
		
		List<TradeSide> sideList = new ArrayList<TradeSide>();
		sideList.addAll(buySideList);
		sideList.addAll(sellSideList);
			
		// order it by reverse time 
		sideList.sort((a, b) -> -a.getTradeDateTime().compareTo(b.getTradeDateTime()));
		
		return sideList.toArray(new TradeSide[sideList.size()]);
	}

	@Override
	public TradeSide[] findMatchedTradeByUserInternal(String userID_) {
		TradeSide[] allTrades = turnTradeToTradeSide(trades.values());
		return Arrays.asList(allTrades).stream().filter(trade -> trade.getTraderID().equals(userID_)).toArray(size -> new TradeSide[size]); 
	}
	
	private PagedMatchedTradeSideList createPagedTradeSideList(List<TradeSide> trades, String cursorString_, int maxNumberOfMatchedTradeSidesOnPage_) {
		int start = Integer.parseInt(cursorString_); 
		int end = (start + maxNumberOfMatchedTradeSidesOnPage_) > trades.size() ? trades.size() : (start + maxNumberOfMatchedTradeSidesOnPage_);
		
		List<TradeSide> sublist = trades.subList(start, end);
		
		PagedMatchedTradeSideList list = new PagedMatchedTradeSideList();
		list.setMatchedTradeSides(sublist.toArray(new TradeSide[sublist.size()]));
		list.setCursorString(""+(end + 1));
		list.setMaxNumberOfMatchedTradeSidesOnPage(sublist.size());
		 
		return list;
	}
}
