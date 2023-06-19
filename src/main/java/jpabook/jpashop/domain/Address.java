package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
public class Address {

	private String city;
	private String street;
	private String zipcode;

	protected Address() {
	} // 불변으로 놔두기 위해 / 누군가 new 를 해서 바꾸면 모든 변수가 다 바뀌니까 setter 도 안함
}
