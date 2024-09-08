package com.polarbookshop.orderservice.order.domain;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // flux는 여러 개의 주문을 위해 사용된다. (0..N)
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 리액티브 스트림의 앞 단계에서 비동기적으로 생성된 주문 객체를 데이터베이스에 저장한다.
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return Mono.just(buildRejectedOrder(isbn, quantity))
                .flatMap(orderRepository::save);
    }

    // 주문이 거부되면 ISBN, 수량, 상태만 지정한다.
    // 스프링 데이터가 식별자, 버전, 감사 메타데이터를 알아서 처리해준다.
    public static Order buildRejectedOrder(String bookIsbn, int quantity) {
        return Order.of(bookIsbn, null, null, quantity, OrderStatus.REJECTED);
    }
}
