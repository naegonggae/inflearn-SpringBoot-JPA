package jpabook.jpashop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Category {

	@Id @GeneratedValue
	@Column(name = "category_id")
	private Long id;

	private String name;

	@ManyToMany // 양방향을 실무에서 사용하지 않는 이유는 category_item 에 FK 두개만들어가고 다른 요소는 들어갈 수 없기 때문 / 그럴바에는 OrderItem 처럼 엔티티하나를 만든다
	@JoinTable(name = "category_item",
			joinColumns = @JoinColumn(name = "category_id"),
			inverseJoinColumns = @JoinColumn(name = "iten_id"))
	private List<Item> items = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Category parent; // 셀프 양방향 걸기 / 대분류 소분류

	@OneToMany(mappedBy = "parent")
	private List<Category> child = new ArrayList<>();

}
