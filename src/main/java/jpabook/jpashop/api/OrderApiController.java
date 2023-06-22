package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jpabook.jpashop.api.OrderSimpleApiController.SimpleOrderDto;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repositiry.OrderRepository;
import jpabook.jpashop.repositiry.OrderSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderRepository orderRepository;

	// 엔티티를 직접 노출해서 사용하면 안되는 방법이다.
	@GetMapping("/api/v1/orders")
	public List<Order> ordersV1() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		for (Order order : orders) {
			order.getMember().getUsername();
			order.getDelivery().getAddress();
			List<OrderItem> orderItems = order.getOrderItems();
			orderItems.stream().forEach(o -> o.getItem().getName()); // 람다형태
//			for (OrderItem orderItem : orderItems) {
//				orderItem.getItem().getName();
//			}
		}
		return orders;
		// json ignore 꼭 해줘
	}

	@GetMapping("/api/v2/orders")
	public List<OrderDto> ordersV2() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDto> collect = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		return collect;
		// DTO 로 반환하라는것은 엔티티를 DTO 에서 사용하는 랩핑도 안되는것임, 왜? 엔티티가 외부에 노출이 되니까
		// 엔티티에 대한 의존을 완전히 끊어야함
		// 그래서 쿼리가 얼마나 나오냐 select 만 아래처럼 11번임
		// -> order, member, delivery, orderItem, item, item, member, delivery, orderItem, item, item
	}

	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3() {
		List<Order> orders = orderRepository.findAllWithItem();
		for (Order order : orders) {
			System.out.println("order ref= " + order + " id =" + order.getId());
		}
		List<OrderDto> collect = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		return collect;
		// JPA 구현체로 Hibernate를 사용하는데, 스프링 부트 3버전 부터는 Hibernate 6 버전을 사용하고 있습니다 :)
		//
		//Hibernate 6버전은 페치 조인 사용 시 자동으로 중복 제거를 하도록 변경되었다고 합니다.
	}

	@Getter
	static class OrderDto {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		private List<OrderItemDto> orderItems; // 엔티티를 랩핑함 // 이거도 DTO 로 접근해야하는데 많이들 실수 한다고 함

		public OrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getUsername(); // LAZY 초기화
			orderDate = order.getOrderDate();
			orderStatus = order.getOrderStatus();
			address = order.getDelivery().getAddress(); // LAZY 초기화
			orderItems = order.getOrderItems().stream()
					.map(o -> new OrderItemDto(o))
					.collect(Collectors.toList());
		}
	}

	@Getter
	static class OrderItemDto {

		private String itemName;
		private int orderPrice;
		private int count;

		public OrderItemDto(OrderItem orderItem) {
			itemName = orderItem.getItem().getName();
			orderPrice = orderItem.getPrice();
			count = orderItem.getCount();
		}
	}

}
