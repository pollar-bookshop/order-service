package com.polarbookshop.orderservice.book;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BoolClient {
    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BoolClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient
                .get()// GET 메서드로 요청
                .uri(BOOKS_ROOT_API + isbn)
                .retrieve()// 요청을 보내고 응답 받는다.
                .bodyToMono(Book.class) // 받은 객체를 Mono<book>으로 반환
                .timeout(Duration.ofSeconds(3), Mono.empty()) // GET 요청에 대해 3초의 타임아웃을 설정, 빈 결과 반환
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))); // 100밀리초가 백오프로 총 3회까지 시도한다.
    }
}
