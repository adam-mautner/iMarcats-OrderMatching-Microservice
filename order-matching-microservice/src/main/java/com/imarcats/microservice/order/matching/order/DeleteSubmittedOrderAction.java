package com.imarcats.microservice.order.matching.order;

import com.imarcats.internal.server.infrastructure.datastore.OrderDatastore;

public class DeleteSubmittedOrderAction {

	private final OrderDatastore orderDatastore;

	public DeleteSubmittedOrderAction(OrderDatastore orderDatastore) {
		super();
		this.orderDatastore = orderDatastore;
	}
	
	public void deleteSubmittedOrder(DeleteSubmittedOrderMessage message) {
		orderDatastore.deleteOrder(message.getOrderKey()); 
	}
	
}
