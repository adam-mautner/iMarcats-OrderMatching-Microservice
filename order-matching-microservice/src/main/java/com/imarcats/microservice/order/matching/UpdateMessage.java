package com.imarcats.microservice.order.matching;

import com.imarcats.microservice.order.matching.market.CallMarketMessage;
import com.imarcats.microservice.order.matching.market.CloseMarketMessage;
import com.imarcats.microservice.order.matching.market.CreateActiveMarketMessage;
import com.imarcats.microservice.order.matching.market.DeleteActiveMarketMessage;
import com.imarcats.microservice.order.matching.market.OpenMarketMessage;
import com.imarcats.microservice.order.matching.order.CreateSubmittedOrderMessage;
import com.imarcats.microservice.order.matching.order.DeleteSubmittedOrderMessage;
import com.imarcats.microservice.order.matching.order.OrderActionMessage;

public class UpdateMessage {

	// Market creation and deletion from Order Matching system
	private CreateActiveMarketMessage createActiveMarketMessage;
	private DeleteActiveMarketMessage deleteActiveMarketMessage;

	// Market Open, Close and Call
	private OpenMarketMessage openMarketMessage;
	private CloseMarketMessage closeMarketMessage;
	private CallMarketMessage callMarketMessage;

	// Order create and delete orders from Order Matching system
	private CreateSubmittedOrderMessage createSubmittedOrderMessage;
	private DeleteSubmittedOrderMessage deleteSubmittedOrderMessage;

	// Order submission and cancellation
	private OrderActionMessage orderActionMessage;

	public CreateActiveMarketMessage getCreateActiveMarketMessage() {
		return createActiveMarketMessage;
	}
	public void setCreateActiveMarketMessage(CreateActiveMarketMessage createActiveMarketMessage) {
		this.createActiveMarketMessage = createActiveMarketMessage;
	}
	public DeleteActiveMarketMessage getDeleteActiveMarketMessage() {
		return deleteActiveMarketMessage;
	}
	public void setDeleteActiveMarketMessage(DeleteActiveMarketMessage deleteActiveMarketMessage) {
		this.deleteActiveMarketMessage = deleteActiveMarketMessage;
	}
	public OrderActionMessage getOrderActionMessage() {
		return orderActionMessage;
	}
	public void setOrderActionMessage(OrderActionMessage orderActionMessage) {
		this.orderActionMessage = orderActionMessage;
	}
	public CreateSubmittedOrderMessage getCreateSubmittedOrderMessage() {
		return createSubmittedOrderMessage;
	}
	public void setCreateSubmittedOrderMessage(CreateSubmittedOrderMessage createSubmittedOrderMessage) {
		this.createSubmittedOrderMessage = createSubmittedOrderMessage;
	}
	public DeleteSubmittedOrderMessage getDeleteSubmittedOrderMessage() {
		return deleteSubmittedOrderMessage;
	}
	public void setDeleteSubmittedOrderMessage(DeleteSubmittedOrderMessage deleteSubmittedOrderMessage) {
		this.deleteSubmittedOrderMessage = deleteSubmittedOrderMessage;
	}
	public OpenMarketMessage getOpenMarketMessage() {
		return openMarketMessage;
	}
	public void setOpenMarketMessage(OpenMarketMessage openMarketMessage) {
		this.openMarketMessage = openMarketMessage;
	}
	public CloseMarketMessage getCloseMarketMessage() {
		return closeMarketMessage;
	}
	public void setCloseMarketMessage(CloseMarketMessage closeMarketMessage) {
		this.closeMarketMessage = closeMarketMessage;
	}
	public CallMarketMessage getCallMarketMessage() {
		return callMarketMessage;
	}
	public void setCallMarketMessage(CallMarketMessage callMarketMessage) {
		this.callMarketMessage = callMarketMessage;
	}
}
