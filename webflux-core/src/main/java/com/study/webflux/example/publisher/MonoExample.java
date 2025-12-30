package com.study.webflux.example.publisher;

import reactor.core.publisher.Mono;


public class MonoExample {
    public static void main(String[] args) {
        Mono<String> mono = Mono.just("Hello");

        mono.subscribe(
                data -> System.out.println("onNext() : " + data),
                error -> System.err.println("onError() : " + error),
                () -> System.out.println("onComplete() : 완료!")
        );
    }
}
