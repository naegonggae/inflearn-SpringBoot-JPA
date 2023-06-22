package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repositiry.OrderRepository;
import jpabook.jpashop.repositiry.OrderSearch;
import jpabook.jpashop.repositiry.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repositiry.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
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
	private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

	@GetMapping("/api/v2/simple-orders")
	public List<SimpleOrderDto> ordersV2() {
		// *총 쿼리는 얼마나 나갈까 N+1
		// Order 1번(order 가 2개면) + 회원 * 2 + 주소 * 2 = 5번
		System.out.println("0======");
		List<Order> orders = orderRepository.findAllByString(new OrderSearch()); // *select 쿼리나감 order 가 두개인걸 인지
		System.out.println("1======");
		List<SimpleOrderDto> collect = orders.stream()
				.map(o -> new SimpleOrderDto(o)) // map 은 A -> B 로 바꾸는것 .map(SimpleOrderDto::new) 람다표현도 가능
				.collect(Collectors.toList());
		System.out.println("2======");
		return collect;
	}

	@GetMapping("/api/v3/simple-orders") // 요거는 엔티티에서 조회하고 DTO 로 바꿈
	public List<SimpleOrderDto> ordersV3() {
		System.out.println("8======");
		List<Order> orders = orderRepository.findAllWithMemberDelivery();
		System.out.println("11======");
		List<SimpleOrderDto> collect = orders.stream()
				.map(o -> new SimpleOrderDto(o))
				.collect(Collectors.toList());
		System.out.println("10======");
		// 8, 쿼리, 11,3,4,5,6,7,3,4,5,6,7,10 끝
		return collect;
	}

	@GetMapping("/api/v4/simple-orders") // 처음부터 DTO 로 가져옴
		public List<OrderSimpleQueryDto> orderV4() {
		return orderSimpleQueryRepository.findOrderDtos();
		// 한방 쿼리는 v3이랑 똑같은데 여기서는 다르게 진짜 필요한거만 뽑아서 select 함 -> 더 최적화됨
	}

	@Data
	static class SimpleOrderDto {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;

		public SimpleOrderDto(Order order) {
			System.out.println("3======");
			orderId = order.getId();
			System.out.println("4======");
			name = order.getMember().getUsername(); // LAZY 초기화 //* member select X 2
			System.out.println("5======");
			orderDate = order.getOrderDate();
			orderStatus = order.getOrderStatus();
			System.out.println("6======");
			address = order.getDelivery().getAddress(); // LAZY 초기화
			// * delivery select X 2 여야하는데 왜 자꾸 delivery 랑 order 은 같이 나가냐 이거땜에 총 7번나가
			// 하이버네이트 문제같다고 말하심
			System.out.println("7======");
		}
	}


}
