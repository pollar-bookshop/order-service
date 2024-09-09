package com.polarbookshop.orderservice.book;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URI;

public class BookClientTests {
    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws Exception {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start(); // 테스트 케이스를 실행하기 앞서 모의 서버를 시작한다.
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.bookClient = new BookClient(webClient);
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown(); // 테스트 케이스가 끝나면 모의 서버를 중지한다.
    }

    @Test
    void whenBookExistsThenReturnBook() {
        var bookIsbn = "1234567890";

        var mockResponse = new MockResponse() // 모의 서버에 의해 반환되는 응답을 정의
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("""
                        {
                            "isbn": %s,
                            "title": "Title",
                            "author": "Author",
                            "price": 9.90,
                            "publisher": "Polarsophia"
                        }
                        """.formatted(bookIsbn));

        mockWebServer.enqueue(mockResponse);

        Mono<Book> book = bookClient.getBookByIsbn(bookIsbn);

        StepVerifier.create(book) // BookClient가 반환하는 객체로 StepVerifier 객체를 초기화한다.
                .expectNextMatches(b -> b.isbn().equals(bookIsbn)) // 반환된 책의 isbn이 요청한 isbn과 같은지 확인한다.
                .verifyComplete(); // 리액티브 스트림이 성공적으로 완료됐는지 확인한다.
    }
}
