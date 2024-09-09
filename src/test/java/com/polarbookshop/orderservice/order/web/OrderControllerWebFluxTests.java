package com.polarbookshop.orderservice.order.web;

import com.polarbookshop.orderservice.order.domain.Order;
import com.polarbookshop.orderservice.order.domain.OrderService;
import com.polarbookshop.orderservice.order.domain.OrderStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(OrderController.class)
public class OrderControllerWebFluxTests {

    @Autowired
    private WebTestClient webClient; // webClient의 변형으로 Restful 서비스 테스트를 쉽게 하기 위한 기능을 추가로 가지고 있다.

    @MockBean
    private OrderService orderService;

    @Test
    void whenBookNotAvailableThenRejectOrder() {
        var orderRequest = new OrderRequest("1234567890", 3);
        var expectedOrder = OrderService.buildRejectedOrder(
                orderRequest.getIsbn(), orderRequest.getQuantity());

        // OrderService 모의 빈이 어떻게 작동해야 하는지 지정한다.
        BDDMockito.given(orderService.submitOrder(
                orderRequest.getIsbn(), orderRequest.getQuantity())
        ).willReturn(Mono.just(expectedOrder));

        webClient
                .post()
                .uri("/orders/")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class).value(actualOrder -> {
                    Assertions.assertThat(actualOrder).isNotNull();
                    Assertions.assertThat(actualOrder.getStatus()).isEqualTo(OrderStatus.REJECTED);
                });
    }
}
