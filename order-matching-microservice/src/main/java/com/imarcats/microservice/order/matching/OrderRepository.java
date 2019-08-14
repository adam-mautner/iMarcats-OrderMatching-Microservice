package com.imarcats.microservice.order.matching;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.imarcats.model.Order;

public interface OrderRepository extends MongoRepository<Order, Long> {
	
}
