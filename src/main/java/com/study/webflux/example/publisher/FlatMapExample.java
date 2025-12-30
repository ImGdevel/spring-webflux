package com.study.webflux.example.publisher;

import java.time.Duration;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FlatMapExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("______Map_____");

        Flux<Integer> flux = Flux.just(1,2,3,4,5)
                .map(x-> x + 1)
                .doOnNext( data ->  System.out.print("x = " + data + " -> "));

        flux.subscribe(
                data->System.out.println( "onNext() : " + data),
                error-> System.out.println("onError() : " + error),
                ()-> System.out.println("onComplete()")

        );


        System.out.println("______FlatMap_____");

        Flux<Integer> flatMapflux = Flux.range(1,10)
                .flatMap(x->
                        Flux.just(x + 2)
                        )
                .doOnNext( data ->  System.out.print("x = " + data + " -> "));


        flatMapflux.subscribe(
                data->System.out.println( "onNext() : " + data),
                error-> System.out.println("onError() : " + error),
                ()-> System.out.println("onComplete()")
        );


        System.out.println("______FlatMap2_____");

        Flux<Integer> flatMapflux2 = Flux.range(1, 10)
                .flatMap(x->
                        Mono.just(x * 10).delayElement(Duration.ofMillis(randomDealy()))
                )
                .doOnNext( data ->  System.out.print("x = " + data + " -> "));


        flatMapflux2.subscribe(
                data->System.out.println( "onNext() : " + data),
                error-> System.out.println("onError() : " + error),
                ()-> System.out.println("onComplete()")
        );

        Thread.sleep(Duration.ofMillis(2000));

        System.out.println("______FlatMap3_____");

        Flux<Integer> flatMapflux3 = Flux.range(1,10)
                .flatMapSequential(x->
                        Mono.just(x * 10).delayElement(Duration.ofMillis(randomDealy()))
                )
                .doOnNext( data ->  System.out.print("x = " + data + " -> "));


        flatMapflux3.subscribe(
                data->System.out.println( "onNext() : " + data),
                error-> System.out.println("onError() : " + error),
                ()-> System.out.println("onComplete()")
        );

        Thread.sleep(Duration.ofMillis(2000));
        
    }

    public static int randomDealy(){
        return ThreadLocalRandom.current().nextInt(100, 1000);
    }

}
