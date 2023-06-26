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
	public List<Order> findAllWithMemberDelivery() { // 많은 API 들이 사용할 수 있음, 재사용성 높다.
		List<Order> resultList = em.createQuery( // 다 땡겨오는 한방 쿼리
				// 프록시말고 진짜 객체 가져오고, 지연로딩 무시하고 바로가져옴
								"select o from Order o" +
								" join fetch o.member m" +
								" join fetch o.delivery d", Order.class)
				.getResultList();

		return resultList;
	}

	public List<Order> findAllWithMemberDelivery(int offset, int limit) { // 많은 API 들이 사용할 수 있음, 재사용성 높다.
		List<Order> resultList = em.createQuery( // 다 땡겨오는 한방 쿼리
						// 프록시말고 진짜 객체 가져오고, 지연로딩 무시하고 바로가져옴
						"select o from Order o" +
								" join fetch o.member m" +
								" join fetch o.delivery d", Order.class) // 여기서 ToOne 관계애들도 없애도 된다.
				// 대신 결과는 Where in 쿼리로 O1 + M1 + D1 + OI1 + I1 쿼리가 나가고 모두 정규화된 상태가 된다.
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();

		return resultList;
	}

	public List<Order> findAllWithItem() {

		List<Order> resultList = em.createQuery(
						"select distinct o from Order o" + // 스프링부트 3부터는 distinct 자동으로 해줌
								// DB 쿼리에서는 distinct 으로 안줄어들어 똑같이 쿼리 조회해보면 주문 4개 나감
								// 여기서 중복제거 해준거는 order id 같길래 걸러서 컬렉션에 담아줌
								" join fetch o.member m" +
								" join fetch o.delivery d" +
								" join fetch o.orderItems oi" +
								" join fetch oi.item i", Order.class)
				.setFirstResult(1)
				.setMaxResults(100)
				// 단점 페이징 불가
				// firstResult/maxResults specified with collection fetch; applying in memory
				// 페이징조건이 페치랑 같이 설정됐다. 그래서 메모리에서 페이징하겠다라는 소리
				// 근데 데이터수가 많은데 메모리에서 페이징하다보면 메모리초과해서 에러가 발생할 수 있다.
				// 이렇게 하는 이유는 DB 에서는 뻥튀기 된 데이터를 받기 때문에 개발자의 의도대로 페이징 처러할 수 없어서 메로리에서 실행시키려한다.
				.getResultList();
		return resultList;
		// DB 는 조인하면 아이템갯수가 총 4개라 4개의 주문이라 인식한다.
		// 사람이 볼때는 2개의 주문에 각각 2개씩 시킨거라 생각하지만
		// 한방쿼리지만 쿼리 결과가 4개로 나옴
	}

	// 결론 : 복잡한 JPQL 과 동적 쿼리를 해결하기 위해서 쿼리 DSL 을 사용하자

	// 우리는 Order 기준으로 select 도하고 페이징도하고 컬렉션을 페치조인도 하길 원하지만 DB 는 갯수가 더 많은 Item 기준으로 해준다..
	// 이건 개발자가 의도한 방향이 아닌데 그러면 어떻게 해야할까? ->

}
