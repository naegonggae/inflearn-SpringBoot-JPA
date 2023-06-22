package jpabook.jpashop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter // setter 는 실무에서 사용안하는 것이 좋다. 어디서 변경되었는지 추적하기 힘듬
public class Member {

	@Id @GeneratedValue
	@Column(name = "member_id")
	private Long id;

	@NotEmpty // Valid 로 컨트롤 가능 / 없으면 Bad Request 400
	private String username;

	@Embedded
	private Address address;

	@OneToMany(mappedBy = "member")
	private List<Order> orders = new ArrayList<>();

}
