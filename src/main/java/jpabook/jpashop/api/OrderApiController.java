package jpabook.jpashop.api;

import java.util.List;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repositiry.OrderRepository;
import jpabook.jpashop.repositiry.OrderSearch;
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

}
