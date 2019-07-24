package com.imarcats.microservice.order.matching;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.imarcats.internal.server.infrastructure.datastore.MarketDatastore;
import com.imarcats.internal.server.infrastructure.datastore.OrderDatastore;
import com.imarcats.internal.server.interfaces.order.OrderInternal;
import com.imarcats.internal.server.interfaces.order.OrderManagementContext;
import com.imarcats.market.engine.matching.OrderCancelActionExecutor;
import com.imarcats.market.engine.matching.OrderSubmitActionExecutor;
import com.imarcats.microservice.order.matching.market.CallMarketAction;
import com.imarcats.microservice.order.matching.market.CloseMarketAction;
import com.imarcats.microservice.order.matching.market.CreateActiveMarketAction;
import com.imarcats.microservice.order.matching.market.DeleteActiveMarketAction;
import com.imarcats.microservice.order.matching.market.OpenMarketAction;
import com.imarcats.microservice.order.matching.order.CreateSubmittedOrderAction;
import com.imarcats.microservice.order.matching.order.DeleteSubmittedOrderAction;
import com.imarcats.microservice.order.matching.order.OrderAction;
import com.imarcats.microservice.order.matching.order.OrderActionMessage;

@Component
public class ActionExecutor implements ConsumerSeekAware {

	// We have to set the topic to the one we set up for Kafka Docker - I know,
	// hardcoded topic - again :)
	public static final String IMARCATS_ORDER_QUEUE = "imarcats_order_q";
	
	// action executors 
	private OrderSubmitActionExecutor orderSubmitActionExecutor;
	private OrderCancelActionExecutor orderCancelActionExecutor;
	private CreateActiveMarketAction createActiveMarketAction;
	private DeleteActiveMarketAction deleteActiveMarketAction;
	private CreateSubmittedOrderAction createSubmittedOrderAction;
	private DeleteSubmittedOrderAction deleteSubmittedOrderAction;
	private OpenMarketAction openMarketAction;
	private CloseMarketAction closeMarketAction;
	private CallMarketAction callMarketAction;

	// offset management 
	private final ThreadLocal<ConsumerSeekCallback> seekCallBack = new ThreadLocal<>();
	private final Map<Integer, Integer> offsetMap = java.util.Collections.synchronizedMap(new HashMap<Integer, Integer>());
	private CountDownLatch initLatch = new CountDownLatch(1);
	
	@Autowired
	@Qualifier("MarketDatastoreImpl")
	protected MarketDatastore marketDatastore;

	@Autowired
	@Qualifier("OrderDatastoreImpl")
	protected OrderDatastore orderDatastore;

	@Autowired
	protected OrderManagementContext orderManagementContext;


	@PostConstruct
	public void postCreate() {
		orderSubmitActionExecutor = new OrderSubmitActionExecutor(marketDatastore, orderDatastore);
		orderCancelActionExecutor = new OrderCancelActionExecutor(marketDatastore, orderDatastore);

		createActiveMarketAction = new CreateActiveMarketAction(marketDatastore);
		deleteActiveMarketAction = new DeleteActiveMarketAction(marketDatastore);

		openMarketAction = new OpenMarketAction(marketDatastore);
		closeMarketAction = new CloseMarketAction(marketDatastore);
		callMarketAction = new CallMarketAction(marketDatastore);

		createSubmittedOrderAction = new CreateSubmittedOrderAction(orderDatastore);
		deleteSubmittedOrderAction = new DeleteSubmittedOrderAction(orderDatastore);
		
		initialize();
	}

	private void initialize() {
		// TODO: Initialize in-memory DBs
		
		// TODO: Initialize Offset Map
		
		initLatch.countDown();
	}
	
	@KafkaListener(topicPartitions = @TopicPartition(topic = IMARCATS_ORDER_QUEUE, partitionOffsets = {
			@PartitionOffset(partition = "0", initialOffset = "0") }))
	@Transactional
	public void listenToOrderActionQueueParition(@Payload UpdateMessage message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {

		try {
			// wait for the init 
			initLatch.await();
			
			// check offset for partition 
			Integer offsetFromMap = offsetMap.get(partition);
			if(offsetFromMap != null) {
				int intendedOffset = offsetFromMap + 1;
				// check, if we should have already consumed the message 
				if (intendedOffset > offset) {
					seekCallBack.get().seek(IMARCATS_ORDER_QUEUE, partition, intendedOffset);
					return;
				}
			}
			
			processMessage(message);
			
			// save the offset for the last successfully processed message 
			offsetMap.put(partition, offset);
			
		} catch (InterruptedException e) {
			// TODO Log it correctly 
			e.printStackTrace();
		}
	}

	private void processMessage(UpdateMessage message) {
		try {
			if (message.getOrderActionMessage() != null) {				
				OrderActionMessage orderActionMessage = message.getOrderActionMessage();
				try {
					if (orderActionMessage.getOrderAction() == OrderAction.Submit) {
						if (!checkStale(orderActionMessage)) {
							orderSubmitActionExecutor.submitOrder(orderActionMessage.getOrderKey(), orderManagementContext);
						}
					} else if (orderActionMessage.getOrderAction() == OrderAction.Cancel) {
						if (!checkStale(orderActionMessage)) {
							orderCancelActionExecutor.cancelOrder(orderActionMessage.getOrderKey(),
									orderActionMessage.getCancellationCommentLanguageKey(), orderManagementContext);
						}
					} else {
						// TODO: Add proper logging
						System.out.println("Unkown action: " + orderActionMessage.getOrderAction());
					}
				} catch (Exception e) {
					// TODO: Add proper logging
					System.out.println("Error processing order action: " + orderActionMessage.getOrderAction() + " for order: "
							+ orderActionMessage.getOrderKey() + ", error: " + e);
					throw e;
				} 
			} else if (message.getCreateSubmittedOrderMessage() != null) {
				createSubmittedOrderAction.createSubmittedOrder(message.getCreateSubmittedOrderMessage());
			} else if (message.getDeleteSubmittedOrderMessage() != null) {
				deleteSubmittedOrderAction.deleteSubmittedOrder(message.getDeleteSubmittedOrderMessage());
			} else if (message.getCreateActiveMarketMessage() != null) {
				createActiveMarketAction.createActiveMarket(message.getCreateActiveMarketMessage());
			} else if (message.getDeleteActiveMarketMessage() != null) {
				deleteActiveMarketAction.deleteActiveMarket(message.getDeleteActiveMarketMessage());
			} else if (message.getOpenMarketMessage() != null) {
				openMarketAction.openMarket(message.getOpenMarketMessage(), orderManagementContext);
			} else if (message.getCloseMarketMessage() != null) {
				closeMarketAction.closeMarket(message.getCloseMarketMessage(), orderManagementContext);
			} else if (message.getCallMarketMessage() != null) {
				callMarketAction.callMarket(message.getCallMarketMessage(), orderManagementContext);
			}
		} catch (Exception e) {
			// critical system error - stop system 

			// TODO: Add proper logging
			System.out.println("Critical error during processing message: " + e + " - stopping Order Matching system");
			System.exit(1); 
		}
	}

	private boolean checkStale(OrderActionMessage message) {
		// we are mostly concerned about resubmitting stale orders. For now, we can be easier on cancellation (we may revisit this later). 
		OrderInternal order = orderDatastore.findOrderBy(message.getOrderKey());
		if (order == null) {
			// non-existent order
			// TODO: Add proper logging
			System.out.println("Non-existent order: " + message.getOrderKey());
			return true;
		}

		return false;
	}

	@Override
	public void registerSeekCallback(ConsumerSeekCallback callback) {
		seekCallBack.set(callback);
	}

	@Override
	public void onPartitionsAssigned(Map<org.apache.kafka.common.TopicPartition, Long> assignments,
			ConsumerSeekCallback callback) {
		// do nothing 
	}

	@Override
	public void onIdleContainer(Map<org.apache.kafka.common.TopicPartition, Long> assignments,
			ConsumerSeekCallback callback) {
		// do nothing
	}

}
