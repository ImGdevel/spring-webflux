package com.study.webflux.reactor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

/**
 * WebFlux 라우터에서 바로 호출할 수 있는 백프레셔 체험용 엔드포인트.
 */
@RestController
@RequestMapping("/api/backpressure")
public class BackpressureDemoController {

	private final BackpressureDemoService service;

	public BackpressureDemoController(BackpressureDemoService service) {
		this.service = service;
	}

	@GetMapping(value = "/limit-rate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> limitRate() {
		return service.limitRateStream();
	}

	@GetMapping(value = "/drop-hot", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> dropHot() {
		return service.dropHotStream();
	}

	@GetMapping(value = "/buffered", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> bufferedBatches() {
		return service.bufferedBatchStream();
	}
}
