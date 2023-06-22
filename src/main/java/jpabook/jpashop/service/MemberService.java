package jpabook.jpashop.service;

import java.util.List;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repositiry.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 조회 메서드의 경우 이런식으로 성능 최적화 할 수 있다.
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	/**
	 * 회원가입
	 */
	@Transactional
	public Long join(Member member) {
		validateDuplicateMember(member);
		System.out.println("3========");
		memberRepository.save(member);
		System.out.println("4========"); //이 메서드 끝나면 insert 쿼리
		return member.getId();
	}

	private void validateDuplicateMember(Member member) {
		System.out.println("1========"); // 다음에 select 쿼리
		// 멀티 쓰레드 환경도 고려해서 member 의 name 에 유니크조건을 준다.
		List<Member> findMember = memberRepository.findByName(member.getUsername());
		if (!findMember.isEmpty()) { // 카운트해서 0보다 크면 에러 터트리면 성능 최적화 할 수 있다.
			throw new IllegalStateException("이미 존재하는 회원입니다.");
		}
		System.out.println("2========");

	}

	// 전체 조회
	public List<Member> findMembers() {
		return memberRepository.findAll();
	}

	public Member findOne(Long memberId) {
		return memberRepository.findOne(memberId);
	}


	@Transactional
	public void update(Long id, String name) {
		System.out.println("1========");

		Member member = memberRepository.findOne(id);
		member.setUsername(name);
		System.out.println("2========");

	}
	// 커맨드와 쿼리는 분리 해야한다.
	// member 를 반환하면 영속상태가 끊긴 객체가 반환된다.
	// void 말고 member 로 리턴값을 주면 이 로직은 변경하는 쿼리를 날리지만 또 이걸 가져와서 조회하는 역할도 하게 된다.
}
