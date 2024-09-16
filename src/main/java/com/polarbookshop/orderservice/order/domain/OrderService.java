package com.polarbookshop.orderservice.order.domain;

import com.polarbookshop.orderservice.book.Book;
import com.polarbookshop.orderservice.book.BookClient;
import com.polarbookshop.orderservice.order.event.OrderDispatchedMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
    private final BookClient bookClient;
    private final OrderRepository orderRepository;

    public OrderService(BookClient boolClient, OrderRepository orderRepository) {
        this.bookClient = boolClient;
        this.orderRepository = orderRepository;
    }

    // flux는 여러 개의 주문을 위해 사용된다. (0..N)
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 리액티브 스트림의 앞 단계에서 비동기적으로 생성된 주문 객체를 데이터베이스에 저장한다.
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn)
                .flatMap(book -> Mono.just(buildAcceptOrder(book, quantity)))
                .switchIfEmpty(Mono.defer(() -> Mono.just(buildRejectedOrder(isbn, quantity)))) // 값이 없을 때 처리
                .flatMap(orderRepository::save);
    }

    public static Order buildAcceptOrder(Book book, int quantity) {
        System.out.println("buildAccepted");
        return Order.of(book.isbn(), book.title() + " - " + book.author(),
                book.price(), quantity, OrderStatus.ACCEPTED);
    }

    // 주문이 거부되면 ISBN, 수량, 상태만 지정한다.
    // 스프링 데이터가 식별자, 버전, 감사 메타데이터를 알아서 처리해준다.
    public static Order buildRejectedOrder(String bookIsbn, int quantity) {
        System.out.println("buildRejected");
        return Order.of(bookIsbn, null, null, quantity, OrderStatus.REJECTED);
    }

    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux) {
        return flux
                .flatMap(message -> orderRepository.findById(message.orderId()))
                .map(this::buildDispatchedOrder) // 주문의 상태를 '배송됨'으로 업데이트한다.
                .flatMap(orderRepository::save);
    }

    private Order buildDispatchedOrder(Order existingOrder) {
        return new Order(
                existingOrder.getId(),
                existingOrder.getBookIsbn(),
                existingOrder.getBookName(),
                existingOrder.getBookPrice(),
                existingOrder.getQuantity(),
                OrderStatus.DISPATCHED,
                existingOrder.getCreatedDate(),
                existingOrder.getLastModifiedDate(),
                existingOrder.getVersion()
        );
    }
}
