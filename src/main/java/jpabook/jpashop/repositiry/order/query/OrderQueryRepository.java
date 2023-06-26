package jpabook.jpashop.repositiry.order.query;

import jakarta.persistence.EntityManager;
import java.util.List;
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
