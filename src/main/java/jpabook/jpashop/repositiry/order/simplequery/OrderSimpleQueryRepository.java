package jpabook.jpashop.repositiry.order.simplequery;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
// 왜 이렇게 옮겼나? -> 리포지토리는 순수한 엔티티를 조회하기 위한 것이기 때문에 DTO 를 조회하는 것을 따로 패키지를 만들어서 관리함
	// 쿼리서비스, 쿼리리포지토리 식으로 패키지를 만들어서 사용
	private final EntityManager em;

	// 사용하는 API 가 제한적이다. fit 하게 만들은 느낌, DTO 로 만들었기 때문에 값을 변경할 수 없다. 엔티티는 가능(V3)
	// 장점은 성능이 좀 더 좋다인데 애플리케이션 네트워크 용량 최적화 정도가 생각보다 미비하다. 근데 재사용성 떨어짐, 코드도 지져분함
	// 리포지토리로 화면을 의존하는 격임, V3 까지는 어쩌어찌 괜찮다 쳐도 / 리포지토리는 엔티티를 조회하는 용도이지 DTO 를 조회하는 용도가 아니다.
	// 그래서 통상적인 상황에서는 v3 가 좋다 하지만 트래픽이 상당하고 필드가 20~30정도 되는 거라면 v4를 고려할만하다.
	public List<OrderSimpleQueryDto> findOrderDtos() {
		List<OrderSimpleQueryDto> resultList = em.createQuery(
						"select new jpabook.jpashop.repositiry.order.simplequery.OrderSimpleQueryDto(o.id, m.username, o.orderDate, o.orderStatus, d.address) "
								+
								" from Order o" +
								" join o.member m" +
								" join o.delivery d", OrderSimpleQueryDto.class)
				.getResultList();
		return resultList;
	}

}
// 쿼리 방식 선택 권장 순서
//1. 우선 엔티티를 DTO 로 변환하는 방법을 선택한다.
//2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
//3. 그래도 안되면 DTO 로 직접 조회하는 방법을 사용한다.
//4. 최후의 방법은 JPA 가 제공하는 네이티브 SQL 이나 스프링 JDBC Template 을 사용해서 SQL 을 직접 사용한다.
