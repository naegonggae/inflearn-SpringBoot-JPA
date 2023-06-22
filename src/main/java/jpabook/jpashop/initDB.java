package jpabook.jpashop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 총 주문 2개
 * * userA
 *    * JPA1 Book
 * 	  * JPA2 Book
 * userB
 *    * SPRING1 Book
 *    * SPRING2 Book
 */
@Component
@RequiredArgsConstructor
public class initDB {

	private final InitService initService;
	@PostConstruct
	public void init() {
		initService.dbInit1();
		initService.dbInit2();
	}

	@Component
	@RequiredArgsConstructor
	@Transactional
	static class InitService {

		private final EntityManager em;
		public void dbInit1() {
			Member member = createMember("userA", "서울", "한강대로", "123-123");
			em.persist(member);

			Book book1 = createBook("JPA1 Book", 10000, 100);
			em.persist(book1);

			Book book2 = createBook("JPA2 Book", 20000, 100);
			em.persist(book2);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		public void dbInit2() {
			Member member = createMember("userB", "대구", "동성로", "123-222");
			em.persist(member);

			Book book1 = createBook("SPRING1 Book", 20000, 200);
			em.persist(book1);

			Book book2 = createBook("SPRING2 Book", 40000, 300);
			em.persist(book2);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		private static Delivery createDelivery(Member member) {
			Delivery delivery = new Delivery();
			delivery.setAddress(member.getAddress());
			return delivery;
		}

		private Member createMember(String name, String city, String street, String zipcode) {
			Member member = new Member();
			member.setUsername(name);
			member.setAddress(new Address(city, street, zipcode));
			return member;
		}

		private static Book createBook(String name, int price, int stockQuantity) {
			Book book1 = new Book();
			book1.setName(name);
			book1.setPrice(price);
			book1.setStockQuantity(stockQuantity);
			return book1;
		}
	}
}
