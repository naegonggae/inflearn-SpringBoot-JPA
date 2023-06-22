package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Jpashop2Application {

	public static void main(String[] args) {
		SpringApplication.run(Jpashop2Application.class, args);
	}

	@Bean
	Hibernate5JakartaModule hibernate5Module() {
		Hibernate5JakartaModule hibernate5JakartaModule = new Hibernate5JakartaModule();
		//강제 지연 로딩 설정
//		hibernate5JakartaModule.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING,
//				true); 이거 끄고도 지연로딩 문제 해결 가능
		return hibernate5JakartaModule;
	}
}
