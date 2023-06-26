package jpabook.jpashop.service.query;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class OrderQueryService {

	// open-in-view 해결방법
	// 컨트롤러나 뷰 템플릿에 있는 지연로딩이나 프록시 초기화등을 서비스단에서 실행하고 컨트롤러와 뷰에서는 호출만 하게 한다.

}
