package jpabook.jpashop.repositiry.order.query;

import java.time.LocalDateTime;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

@Data
public class OrderFlatDto {

	private Long id;
	private String name;
	private LocalDateTime orderDate;
	private OrderStatus orderStatus;
	private Address address;

	private String itemName;
	private int orderPrice;
	private int count;

	public OrderFlatDto(Long id, String name, LocalDateTime orderDate, OrderStatus orderStatus,
			Address address, String itemName, int orderPrice, int count) {
		this.id = id;
		this.name = name;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.address = address;
		this.itemName = itemName;
		this.orderPrice = orderPrice;
		this.count = count;
	}
}
