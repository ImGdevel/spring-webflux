package com.study.webflux.memo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 간단한 메모 CRUD 흐름을 보여주는 WebFlux REST 컨트롤러 예제.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/memos")
public class MemoController {

	private final MemoRepository memoRepository;

	/**
	 * 요청 JSON을 받아 메모를 저장하는 예제 POST 엔드포인트.
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public Mono<Memo> create(@Valid @RequestBody MemoRequest request) {
		return memoRepository.save(request.content());
	}

	/**
	 * 저장된 모든 메모를 리액티브 스트림으로 내려주는 GET 엔드포인트.
	 */
	@GetMapping
	public Flux<Memo> findAll() {
		return memoRepository.findAll();
	}

	/**
	 * ID에 해당하는 단건 메모를 조회하는 GET 엔드포인트.
	 */
	@GetMapping("/{id}")
	public Mono<Memo> findById(@PathVariable Long id) {
		return memoRepository.findById(id);
	}
}
