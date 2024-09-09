package com.polarbookshop.orderservice.book;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .bodyToMono(Book.class); // 받은 객체를 Mono<book>으로 반환
    }
}
