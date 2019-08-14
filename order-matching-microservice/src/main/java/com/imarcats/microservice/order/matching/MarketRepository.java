package com.imarcats.microservice.order.matching;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.imarcats.model.Market;

public interface MarketRepository extends MongoRepository<Market, String> {
	
}
