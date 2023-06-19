package jpabook.jpashop.repositiry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

	@PersistenceContext
	private EntityManager em;

	public void save(Member member) {
		em.persist(member);
	}

	public void findOne(Long id) {
		em.find(Member.class, id);
	}

	public List<Member> findAll(Member member) {
		return em.createQuery("SELECT m from Member m", Member.class)
				.getResultList();
	}

	public List<Member> findByName(String name) {
		return em.createQuery("SELECT m from Member m where m.username = :name", Member.class)
				.setParameter("name", name)
				.getResultList();
	}

}
