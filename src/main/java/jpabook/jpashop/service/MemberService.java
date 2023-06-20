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
		memberRepository.save(member);
		return member.getId();
	}

	private void validateDuplicateMember(Member member) {
		// 멀티 쓰레드 환경도 고려해서 member 의 name 에 유니크조건을 준다.
		List<Member> findMember = memberRepository.findByName(member.getUsername());
		if (!findMember.isEmpty()) { // 카운트해서 0보다 크면 에러 터트리면 성능 최적화 할 수 있다.
			throw new IllegalStateException("이미 존재하는 회원입니다.");
		}
	}

	// 전체 조회
	public List<Member> findMembers() {
		return memberRepository.findAll();
	}

	public Member findOne(Long memberId) {
		return memberRepository.findOne(memberId);
	}


}
