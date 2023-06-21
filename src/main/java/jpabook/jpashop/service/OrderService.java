package jpabook.jpashop.service;

import java.util.List;
import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repositiry.ItemRepository;
import jpabook.jpashop.repositiry.MemberRepository;
import jpabook.jpashop.repositiry.OrderRepository;
import jpabook.jpashop.repositiry.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final MemberRepository memberRepository;
	private final ItemRepository itemRepository;

	// 주문
	@Transactional
	public Long order(Long memberId, Long itemId, int count) { // 이렇게 식별자로 넘기는게 좋다. 그냥 member 이렇게 넘기면 영속성상태가 아닌게 들어가기 때문에
		// 엔티티 조회
		Member member = memberRepository.findOne(memberId);
		Item item = itemRepository.findOne(itemId);

		// 배송정보 생성
		Delivery delivery = new Delivery();
		delivery.setAddress(member.getAddress()); //*

		// 주문 상품 생성
		OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count); //*

//		OrderItem orderItem1 = new OrderItem();
//		orderItem1.setItem(item);
		// createOrderItem 안쓰고 이렇게 하려고 할 수도 있다. 하지만 이렇게 하면 유지보수가 힘들기 때문에
		// OrderItem 에 기본생성자를 protected 로 선언해서 접근을 막아야 한다.

		// 주문 생성
		Order order = Order.createOrder(member, delivery, orderItem);

		// 주문 저장
		orderRepository.save(order); // *표시들은 repository 파서 persist() 해서 DB에 반영해야 할텐데 order 만 저장하면 끝이냐?
		// 이유는 Order 클래스에 Delivery 와 OrderItem 은 cascade = all 되어 있기 때문이다.
		// 그럼 cascade 는 어디까지 사용가능한가?
		// 해당 클래스의 라이프사이클을 관리가능한지를 따져보고 쓰자, 다른것이 참조하지 않는 private Owner 일때 사용
		// 즉, Delivery, OrderItem 은 Order 만 사용하니까 cascade 가 가능함

		return order.getId();
	}

	// 취소
	@Transactional
	public void orderCancel(Long orderId) {
		// 주문 조회
		Order order = orderRepository.findOne(orderId);

		// 주문 취소
		order.cancel();

		// JPQL 을 사용하면 더티체킹을 해서 변경된 데이터값을 직접 쿼리를 날릴 필요없이 JPA 가 로직 변경된걸 감지하고 알아서 update 쿼리를 날린다.
	}

	// 검색
	public List<Order> findOrders(OrderSearch orderSearch) {
		return orderRepository.findAllByString(orderSearch);
	}

}
