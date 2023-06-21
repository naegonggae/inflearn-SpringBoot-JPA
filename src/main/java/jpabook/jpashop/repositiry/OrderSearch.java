package jpabook.jpashop.repositiry;

import jpabook.jpashop.domain.OrderState;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderSearch {
	private String memberName; // 회원이름
	private OrderState orderStatus; // OrderState: ORDER, CANCEL

}
