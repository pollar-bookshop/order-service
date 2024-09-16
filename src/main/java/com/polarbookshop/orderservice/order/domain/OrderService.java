package com.polarbookshop.orderservice.order.domain;

import com.polarbookshop.orderservice.book.Book;
import com.polarbookshop.orderservice.book.BookClient;
import com.polarbookshop.orderservice.order.event.OrderAcceptedMessage;
import com.polarbookshop.orderservice.order.event.OrderDispatchedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final BookClient bookClient;
    private final OrderRepository orderRepository;
    private final StreamBridge streamBridge;

    public OrderService(BookClient boolClient, StreamBridge streamBridge, OrderRepository orderRepository) {
        this.bookClient = boolClient;
        this.orderRepository = orderRepository;
        this.streamBridge = streamBridge;

    }

    // flux는 여러 개의 주문을 위해 사용된다. (0..N)
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 리액티브 스트림의 앞 단계에서 비동기적으로 생성된 주문 객체를 데이터베이스에 저장한다.
    @Transactional
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn)
                .flatMap(book -> Mono.just(buildAcceptOrder(book, quantity)))
                .switchIfEmpty(Mono.defer(() -> Mono.just(buildRejectedOrder(isbn, quantity)))) // 값이 없을 때 처리
                .flatMap(orderRepository::save)
                // 주문이 접수되면 이벤트를 발행한다.
                .doOnNext(this::publishOrderAcceptedEvent);
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

    public void publishOrderAcceptedEvent(Order order) {
        if (!order.status.equals(OrderStatus.ACCEPTED)) {
            return; // 주문의 상태가 ACCEPTED가 아니면 아무것도 하지 않는다.
        }
        var orderAcceptedMessage = new OrderAcceptedMessage(order.getId());
        log.info("Sending order accepted event with id: {}", order.getId());
        // 메시지를 acceptOrder-out-0 바인딩에 명시적으로 보낸다.
        var result = streamBridge.send("acceptOrder-out-0", orderAcceptedMessage);
        log.info("Result of sending data for order with id {}: {}", order.getId(), result);
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
