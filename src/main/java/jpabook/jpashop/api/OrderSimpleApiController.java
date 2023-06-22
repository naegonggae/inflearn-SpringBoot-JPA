package jpabook.jpashop.api;

import java.util.List;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repositiry.OrderRepository;
import jpabook.jpashop.repositiry.OrderSearch;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * xToOne (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

	private final OrderRepository orderRepository;

	@GetMapping("/api/v1/simple-orders")
	public List<Order> ordersV1() {
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		for (Order order : all) {
			order.getMember().getUsername(); // Lazy 강제 초기화
			order.getDelivery().getAddress();
			// order.getMember() <- 여기까지는 프록시 였는데 .getUsername() <- 이후 요 작업을 하면서 DB 에서 직접꺼내와야함으로 진짜 객체로 바뀐다.
			// 즉, Hibernate5JakartaModule 강제 지연로딩 해제 쓰지않고도 해결가능하긴하다.
			// 그렇다고 패치전략을 EAGER 로 바꾸는건 좋지 않다 다른 곳에서도 사용하기 때문
		}
		return all;
		// 문제점 1. 무한 루프를 돈다. order -> member -> order -> member ... / 그래서 양방향 매핑 한쪽에 JsonIgnore 해줌
		// 문제점 2. 이제 매핑된 정보에 접근하려하는데 fetch 전략이 LAZY 이므로 bytebuddy 즉, 프록시 객체를 만나서 에러가 난다.
		// - 그래서 Hibernate5JakartaModule 의존성 주입받음 = 지연로딩 무시하라는 의존성
		// {
		// "id": 1,
		// "member": null,
		// "orderItems": null,
		// "delivery": null,
		// "orderDate": "2023-06-22T16:11:01.409124",
		// "orderStatus": "ORDER",
		// "totalPrice": 50000
		// } // 요따구로 null 값이 나온다. 왜냐? 지연로딩이라 못불러왔기 때문에
		// -> 강제 지연로딩 설정까지 해주면 나오긴함

		// 결론 엔티티쓰니까 내가 궁금한 필드 외에 다른 필드들도 조회를 하게 되어 성능이 나빠지고, 모든 필드들이 노출됨
	}


}
