package jpabook.jpashop.repositiry.order.query;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class OrderQueryDto {

	private Long id;
	private String name;
	private LocalDateTime orderDate;
	private OrderStatus orderStatus;
	private Address address;
	private List<OrderItemQueryDto> orderItems;

	public OrderQueryDto(Long id, String name, LocalDateTime orderDate, OrderStatus orderStatus,
			Address address) {
		this.id = id;
		this.name = name;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.address = address;
	}

	public OrderQueryDto(Long id, String name, LocalDateTime orderDate, OrderStatus orderStatus,
			Address address, List<OrderItemQueryDto> orderItems) {
		this.id = id;
		this.name = name;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.address = address;
		this.orderItems = orderItems;
	}
}
