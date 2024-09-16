package com.polarbookshop.orderservice.order.event;

import com.polarbookshop.orderservice.order.domain.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

// 이 함수는 메시지를 수신하고 데이터베이스 엔티티를 업데이트 할 Cunsumer가 된다.
@Configuration
public class OrderFunctions {

    private static final Logger log = LoggerFactory.getLogger(OrderFunctions.class);

    @Bean
    public Consumer<Flux<OrderDispatchedMessage>> dispatchOrder(OrderService orderService) {
        return flux ->
                // 각 발송된 메시지에 대해 데이터베이스의 해당 주문을 업데이트한다.
                orderService.consumeOrderDispatchedEvent(flux)
                        .doOnNext(order -> log.info("The order with id {} is disaptched", order.getId()))
                        // 리액티브 스트림을 활성화하기 위해 구독한다. 구독자가 없으면 스트림을 통해 데이터가 흐르지 않는다.
                        .subscribe();
    }
}
