package jpabook.jpashop.service;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderState;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repositiry.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
// 사실 DB, Spring 등 의존하는거 없이 독립적으로 테스트를 해야함, 따라서 지금 테스트 로직이 좋은 테스트라고 할 수 없음
// 이건 JPA 랑 스프링부트랑 이렇게 통합적으로 테스트할 수 있구나 보여줌
class OrderServiceTest {

	@Autowired EntityManager em;
	@Autowired OrderService orderService;
	@Autowired OrderRepository orderRepository;

	@Test
	void 상품주문() {
		//given
		Member member = createMember();
		Book book = createBook(10000, 10, "시골 JPA");

		int orderCount = 2;

		//when
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

		//then
		Order getOrder = orderRepository.findOne(orderId);

		assertEquals(OrderState.ORDER, getOrder.getOrderState());
		assertEquals(1, getOrder.getOrderItems().size());
		assertEquals(10000 * orderCount, getOrder.getTotalPrice());
		assertEquals(8, book.getStockQuantity());
	}

	@Test
	void 상품주문_재고수초과() {
		//given
		Member member = createMember();
		Item item = createBook(10000, 10, "시골 JPA");
		int orderCount = 11;

		//when
		try {
			orderService.order(member.getId(), item.getId(), orderCount);
		} catch (NotEnoughStockException e) {
			return;
		}

		//then
		fail("재고 수량 부족 예외가 떠야한다.");
	}

	@Test
	void 주문_취소() {
		//given
		Member member = createMember();
		Item item = createBook(10000, 10, "시골 JPA");
		int orderCount = 2;

		Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

		//when
		orderService.orderCancel(orderId);

		//then
		Order getOrder = orderRepository.findOne(orderId);
		assertEquals(OrderState.CANCEL, getOrder.getOrderState());
		assertEquals(10, item.getStockQuantity());
	}

	private Book createBook(int price, int stockQuantity, String name) {
		Book book = new Book();
		book.setName(name);
		book.setPrice(price);
		book.setStockQuantity(stockQuantity);
		em.persist(book);
		return book;
	}

	private Member createMember() {
		Member member = new Member();
		member.setUsername("회원1");
		member.setAddress(new Address("서울", "강가", "123-123"));
		em.persist(member);
		return member;
	}

}