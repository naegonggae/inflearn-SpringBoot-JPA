package jpabook.jpashop.repositiry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

	private final EntityManager em;

	public void save(Order order) {
		em.persist(order);
	}

	public Order findOne(Long id) {
		return em.find(Order.class, id);
	}

	public List<Order> findAllByString(OrderSearch orderSearch) {

		// 실무에서는 동적쿼리를 이렇게 대응하지 않는다.
		String Jpql = "select o from Order o join o.member m";
		boolean isFirstCondition =true;

		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			if (isFirstCondition) {
				Jpql += " where";
				isFirstCondition = false;
			} else {
				Jpql += " and";
			}
			Jpql += " o.orderStatus = :status";
		}
		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			if (isFirstCondition) {
				Jpql += " where";
				isFirstCondition = false;
			} else {
				Jpql += " and";
			}
			Jpql += " m.username like :name";
		}
		TypedQuery<Order> query = em.createQuery(Jpql, Order.class) .setMaxResults(1000); //최대 1000건
		if (orderSearch.getOrderStatus() != null) {
			query = query.setParameter("status", orderSearch.getOrderStatus());
		}
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			query = query.setParameter("name", orderSearch.getMemberName());
		}
		return query.getResultList();
	}

	public List<Order> findAllByCriteria(OrderSearch orderSearch) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> o = cq.from(Order.class);
		Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
		List<Predicate> criteria = new ArrayList<>();
		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			Predicate status = cb.equal(o.get("status"),
					orderSearch.getOrderStatus());
			criteria.add(status);
		}
		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			Predicate name =
					cb.like(m.<String>get("name"), "%" +
							orderSearch.getMemberName() + "%");
			criteria.add(name);
		}
		cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
		TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
		return query.getResultList();
	}

	// 페치 조인 전략
	// 사실 fetch 라는 명령어는 SQL 에 없다. JPA 기술임
	// 실무에서 성능문제는 90% 가 N+1 문제다 나머지가 10%...
	public List<Order> findAllWithMemberDelivery() {
		List<Order> resultList = em.createQuery( // 다 땡겨오는 한방 쿼리
				// 프록시말고 진짜 객체 가져오고, 지연로딩 무시하고 바로가져옴
								"select o from Order o" +
								" join fetch o.member m" +
								" join fetch o.delivery d", Order.class)
				.getResultList();

		return resultList;
	}

	// 결론 : 복잡한 JPQL 과 동적 쿼리를 해결하기 위해서 쿼리 DSL 을 사용하자

}
