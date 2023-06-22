package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	private String username; // 엔티티를 직접 호출해서 사용하면 이 필드가 바꼈을때 이걸 사용하는 모든 api 에 문제가 생긴다.

	@Embedded
	private Address address;

	// @JsonIgnore // 응답할때 아래필드가 빠짐 / 다른곳은 이 필드를 원할수도 있음 / 그리고 엔티티에 화면을 위한 설정이 있음
	//@JsonIgnore // 양방향 매핑이면 어디 한쪽은 해줘야 무한루프에 안걸림
	@OneToMany(mappedBy = "member")
	private List<Order> orders = new ArrayList<>();

}
