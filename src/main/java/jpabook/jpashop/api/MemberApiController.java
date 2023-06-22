package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController // = @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {

	private final MemberService memberService;

	@PostMapping("/api/v1/members")
	public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
		// @RequestBody = json 온 바디를 그대로 매핑해줌, json 데이터를 멤버로 바꿔줌
		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}

	@PostMapping("/api/v2/members")
	public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
		Member member = new Member();
		member.setUsername(request.getName());
		System.out.println("0========");
		Long id = memberService.join(member);
		System.out.println("5========");
		return new CreateMemberResponse(id);
	}

	@PutMapping("/api/v2/members/{id}")
	public UpdateMemberResponse updateMemberV2(
			@PathVariable("id") Long id,
			@RequestBody @Valid UpdateMemberRequest request) {

		memberService.update(id, request.name); // 수정 로직에서 굳이 member 를 반환하지 않고 조회하는 메서드로 객체를 불러오자.
		System.out.println("3========");
		Member findMember = memberService.findOne(id);
		System.out.println("4========");
		return new UpdateMemberResponse(findMember.getId(), findMember.getUsername());

		// update 메서드의 트랜잭션 어노테이션이 끝나는 지점은 어딜까...? -> 메서드 실행 종료
		// update 메소드에서 select 하고 update 날리고 종료(변경감지로 commit 될때 알아서 update 한거고) -> 컨트롤러 와서 select 는 왜 안할까?

	}

	@Data
	static class CreateMemberResponse {
		private Long id;

		public CreateMemberResponse(Long id) {
			this.id = id;
		}
	}

	@Data
	static class CreateMemberRequest {
		private String name;
	}

	@Data
	@AllArgsConstructor
	static class UpdateMemberResponse {
		private Long id;
		private String name;
	}

	@Data
	static class UpdateMemberRequest {
		private String name;
	}
}
