package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

//@BatchSize(size = 150) 근데 굳이 안쓴다고함
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

	@Id @GeneratedValue
	@Column(name = "order_item_id")
	private Long id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id")
	private Item item;

	private int price; // 주문 가격
	private int count; // 주문 수량

//	protected OrderItem() { // 접근 못하도록 막음, createOrderItem 를 사용하는 쪽으로 유도
//	}

	// 생성 메서드
	public static OrderItem createOrderItem(Item item, int price, int count) {
		OrderItem orderItem = new OrderItem();
		orderItem.setItem(item);
		orderItem.setPrice(price);
		orderItem.setCount(count);

		item.removeStock(count);
		return orderItem;
	}

	// 비즈니스 로직
	public void cancel() { // 주문 취소시 재고 수량 원복
		getItem().addStock(count);
	}

	// 조회 로직

	/**
	 * 주문 상품 전체 가격 조회
	 */
	public int getTotalPrice() {
		return getPrice() * getCount();
	}
}
