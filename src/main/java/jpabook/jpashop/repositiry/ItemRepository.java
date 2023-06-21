package jpabook.jpashop.repositiry;

import jakarta.persistence.EntityManager;
import java.util.List;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

	private final EntityManager em;

	public void save(Item item) {
		if (item.getId() == null) { // 신규 아이템
			em.persist(item);
		} else { // 기존에 DB에 저장되었던 아이템 / update 한다.
			em.merge(item);
		}
	}

	public Item findOne(Long id) {
		return em.find(Item.class, id);
	}

	public List<Item> findAll() {
		return em.createQuery("select i from Item i", Item.class)
				.getResultList();
	}
}
