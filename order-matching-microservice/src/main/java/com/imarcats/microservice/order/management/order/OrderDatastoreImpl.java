package com.imarcats.microservice.order.management.order;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.imarcats.internal.server.infrastructure.datastore.OrderDatastore;
import com.imarcats.internal.server.interfaces.order.OrderInternal;
import com.imarcats.market.engine.order.OrderImpl;
import com.imarcats.microservice.order.management.market.MarketDatastoreImpl;
import com.imarcats.model.Order;
import com.imarcats.model.types.PagedOrderList;

@Component("OrderDatastoreImpl")
public class OrderDatastoreImpl implements OrderDatastore {

	private static final List<String> ACTIVE_ORDER_STATE = Arrays.asList("PendingSubmit", "WaitingSubmit", "Submitted");
	
	private HashMap<Long, Order> orders = new HashMap<Long, Order>();
	private AtomicLong transactionId = new AtomicLong();
	
	@Autowired
	private MarketDatastoreImpl marketDatastore;

	@PostConstruct
	public void postCreate() {
		marketDatastore.setOrderDatastore(this);
	}
	
	@Override
	public Long createOrder(Order order) {
		checkReferenceUniqueness(order);
		long orderIdValue = transactionId.incrementAndGet();
		order.setKey(orderIdValue);
		return orders.put(orderIdValue, order).getKey();
	}

	private void checkReferenceUniqueness(Order order) {
		if(findOrderBy(order.getExternalOrderReference(), order.getSubmitterID(), order.getTargetMarketCode()) != null) {
			throw new RuntimeException("Non-Unique external reference: " + order);
		}
	}
	
	@Override
	public void deleteOrder(Long orderKey) {
		orders.remove(orderKey); 
	}
	
	@Override
	public OrderInternal findOrderBy(Long orderKey) {
		Order order = orders.get(orderKey);
		OrderInternal orderInternal = toOrderInternal(order);
		return orderInternal;
	}

	@Override
	public void deleteNonActiveOrdersOnMarket(String marketCode) {
		Arrays.stream(findNonActiveOrdersOnMarket(marketCode)).forEach(o -> deleteOrder(o.getKey()));
	}

	@Override
	public PagedOrderList findActiveOrdersFromCursorBy(String userID_, String cursorString_, int maxNumberOfOrderOnPage_) {
		return createPagedOrderList(findActiveOrders().filter(order -> order.getSubmitterID().equals(userID_)).collect(Collectors.toList()), cursorString_, maxNumberOfOrderOnPage_);
	}

	private Stream<Order> findActiveOrders() {
		return orders.values().stream().filter(order -> ACTIVE_ORDER_STATE.contains(order.getState()));
	}
	private Stream<Order> findNoActiveOrders() {
		return orders.values().stream().filter(order -> !ACTIVE_ORDER_STATE.contains(order.getState()));
	}

	@Override
	public OrderInternal[] findActiveOrdersOnMarket(String marketCode_) {
		return toOrderInternalArray(findActiveOrders().filter(order -> order.getTargetMarketCode().equals(marketCode_)).collect(Collectors.toList()));
	}

	@Override
	public OrderInternal[] findNonActiveOrdersOnMarket(String marketCode_) {
		return toOrderInternalArray(findNoActiveOrders().filter(order -> order.getTargetMarketCode().equals(marketCode_)).collect(Collectors.toList()));
	}
	
	@Override
	public OrderInternal findOrderBy(String externalReference_, String userID_, String marketCode_) {
		return toOrderInternal(findOrderByUserAndMarket(userID_, marketCode_).filter(order -> order.getExternalOrderReference().equals(externalReference_)).findFirst().get());
	}

	private Stream<Order> findOrderByUserAndMarket(String userID_, String marketCode_) {
		return findOrderByUser(userID_).filter(order -> order.getTargetMarketCode().equals(marketCode_));
	}

	private Stream<Order> findOrderByUser(String userID_) {
		return orders.values().stream().filter(order -> order.getSubmitterID().equals(userID_));
	}

	@Override
	public OrderInternal[] findOrdersBy(String userID_, String marketCode_) {
		return toOrderInternalArray(findOrderByUserAndMarket(userID_, marketCode_).collect(Collectors.toList()));
	}

	@Override
	public OrderInternal[] findOrdersBy(String userID_) {
		return toOrderInternalArray(findOrderByUser(userID_).collect(Collectors.toList()));
	}

	@Override
	public PagedOrderList findOrdersFromCursorBy(String userID_, String cursorString_, int maxNumberOfOrderOnPage_) {
		return createPagedOrderList(findOrderByUser(userID_).collect(Collectors.toList()), cursorString_, maxNumberOfOrderOnPage_);
	}

	@Override
	public PagedOrderList findOrdersFromCursorBy(String userID_, String marketCode_, String cursorString_, int maxNumberOfOrderOnPage_) {
		return createPagedOrderList(findOrderByUserAndMarket(userID_, marketCode_).collect(Collectors.toList()), cursorString_, maxNumberOfOrderOnPage_);
	}

	private OrderInternal[] toOrderInternalArray(List<Order> orders) {
		return orders.stream().map(o -> toOrderInternal(o)).toArray(size -> new OrderImpl[size]);
	}

	private OrderImpl toOrderInternal(Order order) {
		return order != null ? new OrderImpl(order, marketDatastore) : null;
	}

	private PagedOrderList createPagedOrderList(List<Order> orders, String cursorString_, int maxNumberOfMatchedTradeSidesOnPage_) {
		int start = Integer.parseInt(cursorString_); 
		int end = (start + maxNumberOfMatchedTradeSidesOnPage_) > orders.size() ? orders.size() : (start + maxNumberOfMatchedTradeSidesOnPage_);
		
		List<Order> sublist = orders.subList(start, end);
		
		PagedOrderList list = new PagedOrderList();
		list.setOrders(sublist.toArray(new Order[sublist.size()]));
		list.setCursorString(""+(end + 1));
		list.setMaxNumberOfOrdersOnPage(sublist.size());
		 
		return list;
	}
}
