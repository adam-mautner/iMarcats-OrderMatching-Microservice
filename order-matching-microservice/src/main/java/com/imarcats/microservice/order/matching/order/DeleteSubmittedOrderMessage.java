package com.imarcats.microservice.order.matching.order;

public class DeleteSubmittedOrderMessage {

	private Long orderKey;

	public Long getOrderKey() {
		return orderKey;
	}

	public void setOrderKey(Long orderKey) {
		this.orderKey = orderKey;
	}
}
