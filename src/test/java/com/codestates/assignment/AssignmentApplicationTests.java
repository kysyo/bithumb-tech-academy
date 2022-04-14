package com.codestates.assignment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import java.time.Duration;

@SpringBootTest
class AssignmentApplicationTests {

	// 1. ["Blenders", "Old", "Johnnie"] 와 "[Pride", "Monk", "Walker”] 를 순서대로 하나의 스트림으로 처리되는 로직 검증
	@Test
	public void test1() {
		Flux<String> names1 = Flux.just("Blenders", "Old", "Johnnie")
				.delayElements(Duration.ofSeconds(1));
		Flux<String> names2 = Flux.just("Pride", "Monk", "Walker")
				.delayElements(Duration.ofSeconds(1));
		Flux<String> names = Flux.concat(names1, names2)
				.log();

		StepVerifier.create(names)
				.expectSubscription()
				.expectNext("Blenders", "Old", "Johnnie", "Pride", "Monk", "Walker")
				.verifyComplete();
	}

	//2. 1~100 까지의 자연수 중 짝수만 출력하는 로직 검증
	@Test
	public void test2(){

		Flux<Integer> flux = Flux.range(1,100)
				.filter(i -> i%2 == 0)
				.log();

		StepVerifier.create(flux)
				.expectSubscription()
				.thenConsumeWhile(i -> i%2 == 0)
				.verifyComplete();
	}

	//3. “hello”, “there” 를 순차적으로 publish하여 순서대로 나오는지 검증
	@Test
	public void test3(){
		Flux<String> flux = Flux.just("hello", "there")
				.delayElements(Duration.ofSeconds(1)) // 순서를 맞추기위한 딜레이
				.log();

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext("hello", "there")
				.verifyComplete();
	}


	//4. 아래와 같은 객체가 전달될 때 “JOHN”, “JACK” 등 이름이 대문자로 변환되어 출력되는 로직 검증
	/**
	 * Person("John", "[john@gmail.com](mailto:john@gmail.com)", "12345678")
	 * Person("Jack", "[jack@gmail.com](mailto:jack@gmail.com)", "12345678")
	 */

	@Test
	public void test4(){

		Person person1 = new Person("John", "[john@gmail.com](mailto:john@gmail.com)", "12345678");
		Person person2 = new Person("Jack", "[jack@gmail.com](mailto:jack@gmail.com)", "12345678");

		Flux<Person> flux = Flux.just(person1, person2)
				.map(p->
						new Person(p.getName().toUpperCase(), p.getEmail(), p.getPhoneNum())) // 이름 대문자로 재생성
				.log();

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNextMatches(p -> p.getName().equals("JOHN"))
				.expectNextMatches(p -> p.getName().equals("JACK"))
				.verifyComplete();
	}


	//5. ["Blenders", "Old", "Johnnie"] 와 "[Pride", "Monk", "Walker”]를 압축하여 스트림으로 처리 검증
	@Test
	public void test5(){
		Flux<String> flux1 = Flux.just("Blenders", "Old","Johnnie");
		Flux<String> flux2 = Flux.just("Pride", "Monk","Walker");
		Flux<String> zipFlux = Flux.zip(flux1, flux2, (f1, f2) -> f1 + " " + f2) // zip 사용해서 합쳐놓기
				.log();

		StepVerifier.create(zipFlux)
				.expectSubscription()
				.expectNext("Blenders Pride", "Old Monk", "Johnnie Walker")
				.verifyComplete();
	}


	//6. ["google", "abc", "fb", "stackoverflow”] 의 문자열 중 5자 이상 되는 문자열만 대문자로 비동기로 치환하여 1번 반복하는 스트림으로 처리하는 로직 검증
	// 예상되는 스트림 결과값 ["GOOGLE", "STACKOVERFLOW", "GOOGLE", "STACKOVERFLOW"]

	@Test
	public void test6(){
		Flux<String> flux = Flux.just("google", "abc", "fb", "stackoverflow")
				.filter(s-> s.length() >= 5)
				.flatMap(s-> Flux.just(s.toUpperCase()))
				.publishOn(Schedulers.parallel())
				.repeat(1) // 1번 반복
				.log();

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext("GOOGLE", "STACKOVERFLOW", "GOOGLE", "STACKOVERFLOW")
				.verifyComplete();
	}


}

class Person{
	String name;
	String email;
	String phoneNum;

	public Person(String name, String email, String phoneNum){
		this.name = name;
		this.email = email;
		this.phoneNum = phoneNum;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
