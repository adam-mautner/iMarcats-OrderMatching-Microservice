package com.imarcats.microservice.order.matching;

import org.springframework.data.annotation.Id;

public class OrderQueuePartitionOffset {

	@Id
	private Integer partition;
	private Integer offset;
	
	public OrderQueuePartitionOffset() {
		super();
	}
	
	public OrderQueuePartitionOffset(Integer partition, Integer offset) {
		super();
		this.partition = partition;
		this.offset = offset;
	}
	
	public Integer getPartition() {
		return partition;
	}
	public void setPartition(Integer partition) {
		this.partition = partition;
	}
	public Integer getOffset() {
		return offset;
	}
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
}
