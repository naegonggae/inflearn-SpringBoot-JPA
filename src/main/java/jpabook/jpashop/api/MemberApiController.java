package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
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

		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
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
}