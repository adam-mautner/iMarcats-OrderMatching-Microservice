package com.imarcats.microservice.order.matching;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderQueuePartitionOffsetRepository extends MongoRepository<OrderQueuePartitionOffset, Integer> {
	
}
