package com.polarbookshop.orderservice.order.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("orders")
@Getter
public class Order {
    @Id
    Long id;

    String bookIsbn;
    String bookName;
    Double bookPrice;
    Integer quantity;
    OrderStatus status;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    @Version
    int version;

    public Order(Long id, String bookIsbn, String bookName, Double bookPrice, Integer quantity, OrderStatus status, Instant createdDate, Instant lastModifiedDate, int version) {
        this.id = id;
        this.bookIsbn = bookIsbn;
        this.bookName = bookName;
        this.bookPrice = bookPrice;
        this.quantity = quantity;
        this.status = status;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.version = version;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public static Order of(String bookIsbn, String bookName, Double bookPrice,
                           Integer quantity, OrderStatus status) {
        return new Order(null, bookIsbn, bookName, bookPrice, quantity, status, null, null, 0);
    }
}
