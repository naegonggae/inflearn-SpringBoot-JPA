package jpabook.jpashop.service;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repositiry.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class) //junit 실행할대 spring 이랑 엮어서 실행한다는 의미
@SpringBootTest // Autowired 사용하기 위해 / 스프링컨테이너에서 테스트 하는것임
@Transactional // 테스트에 있는 트랜잭션 어노테이션은 기본적으로 끝날때 롤백함
class MemberServiceTest {

	@Autowired MemberService memberService;
	@Autowired MemberRepository memberRepository;
	@Autowired EntityManager em;

	@Test
//	@Rollback(value = false)
	void 회원가입() {
		//given
		Member member = new Member();
		member.setUsername("Kim");

		//when
		Long savedId = memberService.join(member);

		//then
		em.flush(); // 이러면 insert 문나감
		Assertions.assertEquals(member, memberRepository.findOne(savedId));
	}

	@Test//(expected = IllegalStateException.class) junit4에서 지원함 / 쓰면 try-catch 안써도됨
	void 중복회원_체크(){
		//given
		Member member1 = new Member();
		member1.setUsername("Kim");

		Member member2 = new Member();
		member2.setUsername("Kim");

		//when
		memberService.join(member1);
		try {
			memberService.join(member2); // 예외가 발생해야 한다.
		} catch (IllegalStateException e) {
			return;
		}

		//then
		fail("예외가 발생해야 합니다."); // 여기까지 오면 안되는것임
	}
}