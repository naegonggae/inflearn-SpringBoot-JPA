package jpabook.jpashop.api;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

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
import jpabook.jpashop.repositiry.order.query.OrderFlatDto;
import jpabook.jpashop.repositiry.order.query.OrderItemQueryDto;
import jpabook.jpashop.repositiry.order.query.OrderQueryDto;
import jpabook.jpashop.repositiry.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderRepository orderRepository;
	private final OrderQueryRepository orderQueryRepository;

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
				.collect(toList());
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
				.collect(toList());
		return collect;
		// JPA 구현체로 Hibernate를 사용하는데, 스프링 부트 3버전 부터는 Hibernate 6 버전을 사용하고 있습니다 :)
		//
		//Hibernate 6버전은 페치 조인 사용 시 자동으로 중복 제거를 하도록 변경되었다고 합니다.
	}

	// localhost:8080/api/v3.1/orders?offset=1&limit=100 페이징 아주 잘됨
	// default_batch_fetch_size: 100 이 조건을 해주면 기존에 OrderItem 불러올때 Order 당 아이템 몇개인지 + Item1 + Item2 * 2 했는데
	// Order 불러올때 Order 기준으로 in 쿼리를 날려 주문당 OrderItem 을 한꺼번에 가져온다.
	// 그래서 쿼리는 m, d 페치조인으로 한번에 땡겨오고, orderItems in 조건쿼리로 땡겨오고, item 다 가져오고
	// 1+N+N -> 1+1+1 됐다. 페이징도되고 성능최적화도 됐다. 물론 배치 사이즈가 초과되지 않았을때 / 더 빨리 하려면 레디스써야함
	@GetMapping("/api/v3.1/orders")
	public List<OrderDto> ordersV3_page(
			@RequestParam(value = "offset", defaultValue = "0") int offset,
			@RequestParam(value = "limit", defaultValue = "100") int limit
	) {
		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // ToOne 관계는 Fetch join 으로 해결 페이징에 영향을 주지 않기 때문
//		for (Order order : orders) {
//			System.out.println("order ref= " + order + " id =" + order.getId());
//		}
		List<OrderDto> collect = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(toList());
		return collect;
	}

	@GetMapping("/api/v4/orders")
	public List<OrderQueryDto> ordersV4() {
		return orderQueryRepository.findOrderQueryDtos();
	}

	@GetMapping("/api/v5/orders")
	public List<OrderQueryDto> ordersV5() {
		return orderQueryRepository.findAllByDto_optimization();
	}

	@GetMapping("/api/v6/orders")
	public List<OrderQueryDto> ordersV6() {
//		return orderQueryRepository.findAllByDto_flat(); // OrderFlatDto 형태로 뽑아내기

		// OrderQueryDto 형태로 바꾸기
		List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

		return flats.stream()
				.collect(groupingBy(o -> new OrderQueryDto(o.getId(),
								o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
						mapping(o -> new OrderItemQueryDto(o.getId(),
								o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
				)).entrySet().stream()
				.map(e -> new OrderQueryDto(e.getKey().getId(),
						e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
						e.getKey().getAddress(), e.getValue()))
				.collect(toList());
		// 쿼리 한방임
		// Order 기준으로 페이징 불가능
		//
	}

	// 엔티티는 조회해서 DTO 로 변환하는데 -> ToOne 관계는 패치조인 사용 -> 컬렉션은 배치사이즈조정해서 사용 -> 안되면 DTO 로 뽑아내기
	// -> 네이티브 SQL or JDBC 템플릿 = 이 순서를 따르면 왠만한 성능이 나옴
	// 위와 같이해도 사용자 수가 너무 많으면 캐시데이터를 써야하는데 이때 엔티티를 뽑아서 쓰면 절대 안돼
	// 예를들어 영속성 컨텍스트가 관리하고 있는데 케시데이터에 남아 있고 하면 골치아파짐 -> 무조건 DTO 를 캐싱하자
	// DTO 로 뽑아내는건 거의 V5 방식 사용하긴함

	@Data
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
					.collect(toList());
		}
	}

	@Data
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

// 1대 다 조인을 하면 데이터가 뻥튀기 된다는게 DB 에서 조회할때 Order 보다 OrderItem 이 더 많기 때문에 그 차이만큼 Order 정보는 중복해서 쓰이고
// DB 는 그걸 다 땡겨와야하기 때문에 성능이슈가 있다. 용량도 많아지고
// 배치 사이즈를 지정해서 땡겨오면 정규화가 되서 조회가 되기 때문에 효율적이다. 딱 필요한 정보만 애플리케이션에 전송한다.