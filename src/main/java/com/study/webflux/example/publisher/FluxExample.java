package com.study.webflux.example.publisher;

import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Flux 파이프라인을 구성하고 관찰하는 간단한 진입점 예제입니다.
 */
public class FluxExample {

    public static void main(String[] args) {
        ///  기본적인 Flux 흐름
        Flux<String> wordFlux = Flux.just("Spring", "Reactive", "Flux", "Journey")
            .filter(word -> word.length() >= 6)
            .map(String::toUpperCase)
            .doOnNext(value -> System.out.println("[peek] " + value));

        wordFlux.subscribe(
            value -> System.out.println("수신된 값: " + value),
            error -> System.err.println("스트림 에러: " + error),
            () -> System.out.println("기본 파이프라인 완료.")
        );

        System.out.println("-----");


        /// 딜레이 되는 상황에서의 Flux 동작
        Flux<Integer> countdownFlux = Flux.range(1, 3)
            .delayElements(Duration.ofMillis(100))
            .map(number -> 4 - number)
            .doOnNext(step -> System.out.println("카운트다운 단계: " + step));
        countdownFlux.blockLast();


        System.out.println("-----");



    }
}
