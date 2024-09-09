package com.polarbookshop.orderservice.order.domain;

import com.polarbookshop.orderservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

// 이 테스트는 테스트컨테이너에 기반하므로 도커 엔진이 로컬 환경에서 실행중이어야 한다.
@DataR2dbcTest // R2DBC 컴포넌트에 집중하는 테스트 클래스임을 나타낸다.
@Import(DataConfig.class) // 감사를 활성화하기 위한 R2DBC 설정을 임포트한다.
@Testcontainers // 테스트컨테이너의 자동 시작과 중지를 활성화한다.
public class OrderRepositoryR2dbcTests {

    @Container // 테스트를 위한 PostgreSQL 컨테이너를 식별한다.
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource // 테스트 PostgreSQL 인스턴스에 연결하도록 R2DBC와 플라이웨이 설정을 변경한다.
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
    }

    // 테스트컨테이너가 JDBC와는 다르게 R2DBC에 대해서는 연결 문자열을 제공하지 않기 때문에 연결 문자열을 생성한다.
    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s",
                postgresql.getContainerIpAddress(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.getDatabaseName());
    }

    @Test
    void createRejectedOrder() {
        var rejectedOrder = OrderService.buildRejectedOrder("1234567890", 3);
        StepVerifier
                .create(orderRepository.save(rejectedOrder))
                .expectNextMatches(
                        order -> order.status.equals(OrderStatus.REJECTED)
                ).verifyComplete();
    }
}
