package com.imarcats.microservice.order.matching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;
import com.imarcats.internal.server.infrastructure.datastore.MatchedTradeDatastore;
import com.imarcats.internal.server.infrastructure.datastore.OrderDatastore;
import com.imarcats.internal.server.infrastructure.marketdata.MarketDataSession;
import com.imarcats.internal.server.infrastructure.marketdata.MarketDataSessionImpl;
import com.imarcats.internal.server.infrastructure.marketdata.MarketDataSource;
import com.imarcats.internal.server.infrastructure.marketdata.MarketDataSourceImpl;
import com.imarcats.internal.server.infrastructure.notification.NotificationBroker;
import com.imarcats.internal.server.infrastructure.notification.properties.PropertyChangeBroker;
import com.imarcats.internal.server.infrastructure.notification.properties.PropertyChangeBrokerImpl;
import com.imarcats.internal.server.infrastructure.notification.properties.PropertyChangeSession;
import com.imarcats.internal.server.infrastructure.notification.properties.PropertyChangeSessionImpl;
import com.imarcats.internal.server.infrastructure.notification.trades.TradeNotificationBroker;
import com.imarcats.internal.server.infrastructure.notification.trades.TradeNotificationBrokerImpl;
import com.imarcats.internal.server.infrastructure.notification.trades.TradeNotificationSession;
import com.imarcats.internal.server.infrastructure.notification.trades.TradeNotificationSessionImpl;
import com.imarcats.internal.server.interfaces.order.OrderManagementContext;
import com.imarcats.internal.server.interfaces.order.OrderManagementContextImpl;
import com.imarcats.market.engine.order.OrderManagementSystem;
import com.imarcats.microservice.order.matching.notification.KafkaMessageBroker;
import com.imarcats.microservice.order.matching.notification.NotificationBrokerImpl;

@Configuration
public class OrderManagementSystemFactory {

	@Autowired
	@Qualifier("MarketDatastoreImpl")
	protected MarketDatastore marketDatastore;
	
	@Autowired
	@Qualifier("OrderDatastoreImpl")
	protected OrderDatastore orderDatastore;
	
	@Autowired
	@Qualifier("MatchedTradeDatastoreImpl")
	protected MatchedTradeDatastore tradeDatastore;
	
	@Autowired
	protected KafkaMessageBroker kafkaMessageBroker;
	
	@Bean
	public OrderManagementSystem createOrderManagementSystem() {
		return new OrderManagementSystem(marketDatastore, orderDatastore, tradeDatastore, null, null);
	}
	
	@Bean
	public OrderManagementContext createOrderManagementContext() {
		NotificationBroker broker = new NotificationBrokerImpl(kafkaMessageBroker);
		MarketDataSource marketDataSource = new MarketDataSourceImpl(broker); 
		MarketDataSession marketDataSession = new MarketDataSessionImpl(marketDataSource);
		PropertyChangeBroker propertyChangeBroker = new PropertyChangeBrokerImpl(broker);  
		PropertyChangeSession propertyChangeSession = new PropertyChangeSessionImpl(propertyChangeBroker);
		TradeNotificationBroker tradeNotificationBroker = new TradeNotificationBrokerImpl(broker);  
		TradeNotificationSession tradeNotificationSession = new TradeNotificationSessionImpl(tradeNotificationBroker);
		
		OrderManagementContext context = new OrderManagementContextImpl(
				marketDataSession,
				propertyChangeSession,
				tradeNotificationSession);
		return context;
	}
	
	public static Pageable createPageable(String cursorString_,
			int numberOnPage_) {
		 return PageRequest.of(cursorString_ != null ? Integer.parseInt(cursorString_) : 0, numberOnPage_);
	}
}
