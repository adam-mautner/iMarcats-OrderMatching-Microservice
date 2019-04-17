package com.imarcats.microservice.order.management.order;

import com.imarcats.interfaces.server.v100.dto.mapping.OrderDtoMapping;
import com.imarcats.internal.server.infrastructure.datastore.OrderDatastore;

public class CreateSubmittedOrderAction {

	private final OrderDatastore orderDatastore;

	public CreateSubmittedOrderAction(OrderDatastore orderDatastore) {
		super();
		this.orderDatastore = orderDatastore;
	}
	
	public void createSubmittedOrder(CreateSubmittedOrderMessage message) {
		orderDatastore.createOrder(OrderDtoMapping.INSTANCE.fromDto(message.getOrder()));
	}
	
}
