package jpabook.jpashop.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 코드를 제약하는 스타일로 짜라
public class Order {

	@Id @GeneratedValue
	@Column(name = "order_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // 일대일 매핑의 주인을 어디로 선정할지 고려해야한다.
	private List<OrderItem> orderItems = new ArrayList<>();

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // Order 저장할때 Delivery 도 저장, 삭제할때도 마찬가지
	@JoinColumn(name = "delivery_id")
	private Delivery delivery;

	private LocalDateTime orderDate; // 주문시간

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus; // 주문상태 : ORDER, CANCEL

	// 연관관계 편의 메서드 / 위치는 컨트롤하는 곳에 두면 좋음 / 양방향일 경우 쌍방향으로 저장해주는 역할
	public void setMember(Member member) {
		this.member = member;
		member.getOrders().add(this);
	}

	public void addOrderItem(OrderItem orderItem) {
		orderItems.add(orderItem);
		orderItem.setOrder(this);
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
		delivery.setOrder(this);
	}

	// 생성메서드

	// 주문 같은 경우 복잡한 연관관계를 가지기 때문에 생성메서드가 있으면 좋다.
	// 장점 : 생성관련해서 수정할때 여기만 건드리면 된다.
	// 실무에서 생성메서드는 훨씬 복잡함, OrderItem 으로 안넘기고 DTO 로 넘길거고, 메서드 내부에서 생성자를 통해 생성하는 경우가 있을 수 있음
	public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) { // ... 으로 list 넘김
		Order order = new Order();
		order.setMember(member);
		order.setDelivery(delivery);
		for (OrderItem orderItem : orderItems) {
			order.addOrderItem(orderItem);
		}
		order.setOrderStatus(OrderStatus.ORDER);
		order.setOrderDate(LocalDateTime.now());
		return order;
	}

	// 비즈니스 로직

	/**
	 * 주문취소
	 */
	public void cancel() {
		if (delivery.getStatus() == DeliveryStatus.COMP) {
			throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
		}
		this.setOrderStatus(OrderStatus.CANCEL);
		for (OrderItem orderItem : orderItems) {
			orderItem.cancel();
		}
	}

	// 조회 로직
	/**
	 * 전체 주문 가격 조회
	 */
	public int getTotalPrice() {
		return orderItems.stream()
				.mapToInt(OrderItem::getTotalPrice)
				.sum();

//		int totalPrice = 0;
//		for (OrderItem orderItem : orderItems) {
//			totalPrice += orderItem.getTotalPrice();
//		}
//		return totalPrice;
	}

}
