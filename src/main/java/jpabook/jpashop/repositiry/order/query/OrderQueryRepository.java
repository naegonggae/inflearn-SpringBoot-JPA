package jpabook.jpashop.repositiry.order.query;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;

	// 왜 컨트롤러의 dto 를 참조하지 않고 레포지토리 내부에 하나를 더 팠나? -> 레포지토리가 컨트롤러와 순환 참조하기 때문
	public List<OrderQueryDto> findOrderQueryDtos() {
		List<OrderQueryDto> result = findOrders();

		result.forEach(o->{
			List<OrderItemQueryDto> orderItems = findOrderItems(o.getId());
			o.setOrderItems(orderItems);
		});
		return result;
		// 쿼리는 order 한번, item 1 한번, item 2 한번 == N+1
	}

	public List<OrderQueryDto> findAllByDto_optimization() {
		// 장점 : V4 보다는 데이터 select 하는 양이 적다.
		List<OrderQueryDto> result = findOrders(); //**

		List<Long> orderIds = toOrderIds(result);
		Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap( orderIds);

		result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getId())));

		return result;

	}

	private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
		List<OrderItemQueryDto> orderItems = em.createQuery( //**
						"select new jpabook.jpashop.repositiry.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.price, oi.count)"
								+
								" from OrderItem oi" +
								" join oi.item i" +
								" where oi.order.id in :orderIds", OrderItemQueryDto.class)
				.setParameter("orderIds", orderIds) // orderId 를 찾은거와 관계있는 item 들을 땡겨오는 쿼리
				.getResultList();

		// 메모리에서 값을 매칭해서 찾는 형식임 -> 쿼리가 총 두번나감 ** 에서
		// ordersV4 는 for 문을 돌면서 찾을때마다 쿼리를 날려주는 방법
		Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
				// 키는 아이디, 값은 DTO
				// map 형태로 바꾸기
				.collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
		return orderItemMap;
	}

	private static List<Long> toOrderIds(List<OrderQueryDto> result) {
		List<Long> orderIds = result.stream() // Order id 리스트 만들기 // 원래 여기서 쿼리가 한번나가야하는데 메모리상에서 해결함
				.map(o -> o.getId())
				.collect(Collectors.toList());
		return orderIds;
	}

	private List<OrderItemQueryDto> findOrderItems(Long id) {
		return em.createQuery(
				"select new jpabook.jpashop.repositiry.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.price, oi.count)" +
						" from OrderItem oi" +
						" join oi.item i" +
						" where oi.order.id = :orderId", OrderItemQueryDto.class)
				.setParameter("orderId", id)
				.getResultList();
	}

	private List<OrderQueryDto> findOrders() {
		List<OrderQueryDto> resultList = em.createQuery(
						"select new jpabook.jpashop.repositiry.order.query.OrderQueryDto(o.id, m.username, o.orderDate, o.orderStatus, d.address)" +
								" from Order o" +
								" join o.member m" +
								" join o.delivery d", OrderQueryDto.class)
				.getResultList();
		return resultList;
	}
}
// repository 에 만들지 않고 이렇게 따로 패키지를 파서 만드는 이유는 레포지토리는 엔티티에 접근하는 용도인데 지금만든거는 특정 화면에서 사용되는 접근방식이므로
// 공간을 분리한 것이다. / 관심사 분리 / 핵심로직 vs 화면쿼리
